package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>, IdListMapper<Category, Long> {

    /**
     * 根据category_id删除中间表tb_category_brand相关数据
     * @param cid
     */
    @Delete("DELETE FROM tb_category_brand WHERE category_id = #{cid}")
    void deleteByCategoryIdInCategoryBrand(@Param("cid") Long cid);

    /**
     * 查询最后一条数据
     */
    @Select("SELECT * FROM `tb_category` WHERE id = (SELECT MAX(id) FROM tb_category)")
    List<Category> selectLast();

    /**
     * 根据品牌ID查询商品分类信息
     * @param bid
     */
    @Select("SELECT * FROM tb_category WHERE id IN (SELECT category_id FROM tb_category_brand WHERE brand_id = #{bid})")
    List<Category> queryByBrandId(@Param("bid") Long bid);
}
