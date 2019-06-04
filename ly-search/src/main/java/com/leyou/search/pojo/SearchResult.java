package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 *  返回结果多加了一些属性，就继承PageResult，进行扩展
 * */
@Data
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories;          //分类过滤条件

    private List<Brand> brands;                 //品牌过滤条件

    private List<Map<String, Object>> specs;    //规格参数过滤条件   key以及待选项options

    private SearchResult() {                    //建议都保留一个空参构造，很多界面处理的时候，都会默认利用反射调用空参构造去构造对象，没有可能会导致一些错误

    }

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs){  // 视频里面说参数过多可以用到工厂模式解决，自己研究
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
