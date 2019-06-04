package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void createIndex(){
        //创建索引
        elasticsearchTemplate.createIndex(Goods.class);
        //配置映射
        elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void testDelete(){
        //删除索引
        elasticsearchTemplate.deleteIndex(Goods.class);
    }

    /*
     *  加载数据，把数据库里面的所有spu数据都封装到goods对象，然后倒入到索引库
     * */
    @Test
    public void loadData() {
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            //查询spu信息
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> spuList = result.getItems();

            if (CollectionUtils.isEmpty(spuList)){
                break;
            }

            //构建成为goods
            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //存入索引库
            goodsRepository.saveAll(goodsList);     // 接收对象集合，实现批量新增
            //goodsRepository.save(goods);

            //翻页
            page++;
            size = spuList.size();
        }while(size == 100 );
    }
}