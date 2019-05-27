package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: cuzz
 * @Date: 2018/11/6 19:46
 * @Description: 商品货物
 */
@Slf4j
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    /**
     * 分页查询商品列表
     */
    public PageResult<Spu> querySpuPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 分页,最多允许查100条
        PageHelper.startPage(page, Math.min(rows, 100));

        // 创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // 是否过滤上下架
        if (saleable != null) {
            criteria.orEqualTo("saleable", saleable);
        }

        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }

        // 默认排序
        example.setOrderByClause("last_update_time DESC");

        // 查询
        List<Spu> spus = spuMapper.selectByExample(example);

        // 判断
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        // 解析分类和品牌的名称
        loadCategoryAndBrandName(spus);

        // 解析分页的结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(), spus);
    }

    /*
     * 解析分类和品牌的名称
     */
    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // 处理分类名称
            List<Category> categories = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());      // 这特么啥玩意？
            // java8里面的stream()
            spu.setCname(StringUtils.join(names, "/"));     // 把集合通过/拼成字符串
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    /**
     * 商品新增
     */
    @Transactional
    public void saveGoods(Spu spu) {
        saveSpu(spu);           // 新增spu
        saveSpuDetail(spu);     // 新增detail
        saveSkuAndStock(spu);   // 新增sku和库存

        this.sendMessage(spu.getId(), "insert");     // 发送消息
    }

    /*
     * 新增sku和库存
     */
    private void saveSkuAndStock(Spu spu) {
        List<Stock> list = new ArrayList<>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            // 保存sku
            sku.setSpuId(spu.getId());
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            int count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            list.add(stock);
        }

        // 批量新增库存 新增的逻辑放到for循环里面去处理不好，批量处理好些
        int count = stockMapper.insertList(list);           // 批量新增
        if (count != skus.size()) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    /*
     * 新增spu_detail
     */
    private void saveSpuDetail(Spu spu) {
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        int count = spuDetailMapper.insert(detail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    /*
     * 新增spu
     */
    private void saveSpu(Spu spu) {
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    /**
     * 根据SPUID查询商品细节
     */
    public SpuDetail querySpuDetailByid(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.SPU_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    /**
     * 根据SPU查询下面所有的SKU
     */
    public List<Sku> querySkusBySpuId(Long id) {
        // 查询SKU
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        // 查询库存
//        for (Sku s : skus) {
//            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
//            if (stock == null) {
//                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
//            }
//            s.setStock(stock.getStock());
//        }

        // java8方式查询库存
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        // 我们把stock变成一个map,其中key是：sku的id，值是库存的值
        Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s -> s.setStock(stockMap.get(s.getId())));
        return skus;
    }

    /**
     * 商品修改
     */
    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null) {
            throw new  LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        // 查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            // 删除sku
            skuMapper.delete(sku);
            // 删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        // 修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 新增sku和stock
        saveSkuAndStock(spu);
    }

    public Spu querySpuByid(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new  LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spu;
    }

    // 封装一个发送消息的方法
    private void sendMessage(Long id, String type){
        // 发送消息
        try {
            //this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            log.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }
}
