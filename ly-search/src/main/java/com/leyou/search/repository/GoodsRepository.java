package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
*   Spring Data 的强大之处，就在于你不用写任何DAO处理，自动根据方法名或类的信息进行CRUD操作。
 *   只要你定义一个接口，然后继承Repository提供的一些子接口，就能具备各种基本的CRUD功能。
* */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
