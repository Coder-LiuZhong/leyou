package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @Author: 98050
 * Time: 2018-10-11 17:21
 * Feature:搜索时对应的实体类
 */
@Data
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 0)         // 标记此实体类是文档对象，指定对应的索引库的名称和类型
public class Goods {
    @Id                                 // 标记一个字段作为id主键
    private Long id;                    // spuId

    /**
     * 所有需要被搜索的信息，包含标题，分类，甚至品牌
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")           // 标记为文档的字段，并指定字段映射属性;  字段类型、分词器；还有是否索引
    private String all;                 // 所有需要被搜索的信息，包含标题，分类；  只有它是Text类型，搜索字段，搜索框输入的东西都来这里查，所以你要把商品的信息都放这里。

    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;            // 卖点，京东搜索商品之后，鼠标移动到商品上提示的东西。只是展示给用户看的，不需要索引

    // 下面几个不加注解，Spring也会自动推测；新增的时候貌似可以智能推断
    private Long brandId;       // 品牌id         用来做过滤的，京东搜索后上面会很多品牌的过滤条件
    private Long cid1;          // 1级分类id
    private Long cid2;          // 2级分类id
    private Long cid3;          // 3级分类id       cid其实一个就够了，三个有点多余。是用来展示过滤的，京东搜索后在左上角有商品的分类信息，分了三级，可以选择进行过滤
    private Date createTime;    // 创建时间        过滤条件里面有个新品要用来排序
    private Set<Long> price;    // 价格            价格排序。是所有sku的价格集合。方便根据价格进行筛选过滤

    @Field(type = FieldType.Keyword, index = false)
    private String skus;        // 商品sku的集合，用JSON结构表示。  只是展示不需要过滤，将来取完在页面展示就会非常的方便。
                                // 如果不要这个那这个类就只用来过滤了，不用来展示了。也可以
    private Map<String, Object> specs;    // 可搜索的规格参数，key是参数名，值是参数值     ES能接收对象结构{"机身内存"，"4GB"}  spec.机身内存.keyword:keyword
                                          // 所有规格参数都是不需要分词的，其实  搞成map比较灵活，动态字段;     存入的是{"内存":"4GB"}
}
