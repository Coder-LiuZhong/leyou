package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 把数据库查询出来的商品SPU构建成Goods对象，方便把数据存入ES索引库
     * */
    public Goods buildGoods(Spu spu){
        //构建goods对象
        Goods goods = new Goods();          // 每一个spu对应一个goods

        //查询商品分类名称
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if(CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());

        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //搜索字段     包括分类、品牌
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();

        //查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //Set<Long> priceList = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());      // skuList遍历了两次影响性能，放到一起处理

        //对sku进行处理，仅仅封装id，价格，标题，图片
        List<Map<String, Object>> skus = new ArrayList<>();
        Set<Long> priceList = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));    // 时刻想着用工具类，处理了为空的情况
            skus.add(map);
            // 处理价格
            priceList.add(sku.getPrice());
        }

        //查询规格参数
        List<SpecParam> params = specificationClient.queryParamList(null, spu.getCid3(), true);     // 能搜索的规格参数
        if (CollectionUtils.isEmpty(params)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }

        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spu.getId());

        //获取通用规格参数  里面有商品对应的规格参数的值  返回的是JSON结构，转成map结构
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);   // JSON结构字符转Map，key的类型是Long，value的类型是string

        //获取特有规格参数  返回的值是list而不是上面一样的字符串，JSON字符串转化就要用通用的转化方法nativeRead
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});

        //规格参数，key是规格参数的名字，值是规格参数的值
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            String key = param.getName();       //规格名称
            Object value = "";                  //规格的值

            //判断是否为通用规格
            if(param.getGeneric()){
                //参数是通用属性，通过规格参数的ID从商品详情存储的规格参数中查出值
                value = genericSpec.get(param.getId());
                //判断是否是数值类型
                if(param.getNumeric()&&value!=null){
                    //参数是数值类型，处理成段，方便后期对数值类型进行范围过滤
                    value = chooseSegment(value.toString(), param);
                }
            }else{
                value = specialSpec.get(param.getId());
            }
            //存入map
            specs.put(key, value);
        }

        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);                          //搜索字段，包含标题，分类，品牌，规格等等。     页面关键字搜索查询是去索引库里查这里面的内容
        goods.setPrice(priceList);                  //所有sku的价格集合
        goods.setSkus(JsonUtils.toString(skus));    //所有sku的集合的json形式。  把list<map> 转换成了JSON格式
        goods.setSpecs(specs);                      //所有可以搜索的规格参数； "内存"：4GB 这种map格式
        return goods;
    }

    /*
    * 将具体的规格作为段里面的值存入，方便作为过滤条件
    * */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);       // 将字符串转为double
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {         // p.getSegments()是提前数据库里面设置好了的此规格的段
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;      // double类型的最大值
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索
     */
    public PageResult<Goods> search(SearchRequest searchRequest) {

        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)){
            return null;
        }
        int page = searchRequest.getPage() - 1;     // 当前页。  ES中默认从0开始
        int size = searchRequest.getSize();         // 每页大小

        //创建查询构建器       用nativeSearchQueryBuilder可以组装分页、排序、结果过滤等事儿
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //0.结果过滤        只显示这些字段
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));

        //1.分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page, size));

        //2.搜索条件
        QueryBuilder basicQuery = buildBasicQuery(searchRequest);
        nativeSearchQueryBuilder.withQuery(basicQuery);

        //3.聚合分类和品牌
        //3.1 聚合分类
        String categoryAggName = "category_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));

        //3.2 聚合品牌
        String brandAggName = "brand_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //4.查询
        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        // Template方式更通用，任何实体类都可以，传入字节码就行，而且要想拿到聚合结果，视频说用template更好；repository.search()并没有返回聚合的情况，所以就用template

        //5.解析结果
        //5.1 解析分页结果
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();        // 返回很多null的字段，那就在yml里面配置Jackson，排除空的

        //5.2 解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));        // 这样就知道本次搜索索引的结果里面有多少 商品分类
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));                     // 这样就知道本次搜索索引的结果里面有多少 品牌

        //6 完成规格参数聚合
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1){                              //商品分类存在并且数量为1，可以聚合规格参数； 因为一个商品分类对应了一批商品规格；
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);       //所以页面最好设计为选中分类之后再显示规格参数；或者你前面聚合后选数量最多的那个商品分类进行查询规格显示，一般都是第一个
        }

        return new SearchResult(total, totalPage, goodsList, categories, brands, specs);
    }

    /*
    *  搜索过滤条件封装
    * */
    private QueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        //创建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();          // 搜索条件和过滤条件不能写到一起，需要用bool来分离
        //查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        //过滤条件
        Map<String, String> map = searchRequest.getFilter();
        for (Map.Entry<String, String> entry : map.entrySet()) {    // map.entrySet().for
            String key = entry.getKey();
            if(!"cid3".equals(key) && !"brandId".equals(key)){      // key分两种，商品分类和品牌、其他规格
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }

        return boolQueryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        //1 查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.queryParamList(null, cid, true);
        //2 聚合
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //2.1 带上查询条件
        nativeSearchQueryBuilder.withQuery(basicQuery);
        //2.2 聚合
        for (SpecParam param : params) {
           String name = param.getName();
           nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));        // 不分词，加上keyword属性
        }
        //3 获取结果
        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        //4 解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            //准备map
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            specs.add(map);
        }

        return specs;
    }

    /*
     * 聚合结果里面的key：商品分类ID去得到商品分类信息。
     * */
    private List<Category> parseCategoryAgg(LongTerms longTerms) {      // 聚合方式term，得到的值是Long，所以选LongTerms
        try{
            List<Long> ids = longTerms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        }catch (Exception e){
            log.error("[搜索服务]查询分类异常：", e);
            return null;
        }
    }

    /*
     * 聚合结果里面的key：品牌ID去得到商品分类信息。
     * */
    private List<Brand> parseBrandAgg(LongTerms longTerms){
        try{
            List<Long> ids = longTerms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        }catch(Exception e){
            log.error("[搜索服务]查询品牌异常：", e);
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods对象
        Goods goods = buildGoods(spu);
        //存入到索引库
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}