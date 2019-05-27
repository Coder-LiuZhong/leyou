package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父节点ID查询商品分类
     */
    public List<Category> queryCategoryListByPid(Long pid) {
        if(-1==pid){
            List<Category> last =this.categoryMapper.selectLast();      // 前台传过来-1 就代表是要最后插入的那条数据。
            return last;
        }

        Category t = new Category();
        t.setParentId(pid);
        List<Category> list = categoryMapper.select(t);       // t里的非空字段作为条件

        if(CollectionUtils.isEmpty(list)){          // 用工具类代替   if (list == null || list.isEmpty())
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;
    }

    /**
     * 保存商品分类
     */
    public void saveCategory(Category category) {
        /**
         * 将本节点插入到数据库中
         * 将此category的父节点的isParent设为true
         */
        category.setId(null);                       //1.首先置id为null
        categoryMapper.insert(category);            //2.保存
        Category parent = new Category();           //3.修改父节点，标记为父节点
        parent.setId(category.getParentId());
        parent.setIsParent(true);
        this.categoryMapper.updateByPrimaryKeySelective(parent);            // updateByPrimaryKey对你注入的字段全部更新
        // updateByPrimaryKeySelective会对字段进行判断再更新(如果为Null就忽略更新)，如果你只想更新某一字段，可以用这个方法。
    }

    /**
     * 删除商品分类
     */
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectByPrimaryKey(id);
        if(category.getIsParent()){
            List<Category> list = new ArrayList<>();
            queryAllLeafNode(category,list);        //1.查找所有叶子节点

            List<Category> list2 = new ArrayList<>();
            queryAllNode(category,list2);           //2.查找所有子节点

            for (Category c:list2){
                this.categoryMapper.delete(c);      //3.删除tb_category中的数据,使用list2
            }

            for (Category c:list){
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(c.getId());       //4.维护中间表,父节点的都不会存在于中间表，只有最底层的分类才会有对应的品牌
            }
        }else {
            //1.查询此节点的父亲节点的孩子个数 ===> 查询还有几个兄弟
            Example example = new Example(Category.class);
            example.createCriteria().andEqualTo("parentId",category.getParentId());
            List<Category> list = this.categoryMapper.selectByExample(example);
            if(list.size()!=1){
                this.categoryMapper.deleteByPrimaryKey(category.getId());                   //有兄弟,直接删除自己
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());    //维护中间表
            }else {
                this.categoryMapper.deleteByPrimaryKey(category.getId());                   //已经没有兄弟了
                Category parent = new Category();
                parent.setId(category.getParentId());
                parent.setIsParent(false);
                this.categoryMapper.updateByPrimaryKeySelective(parent);
                //维护中间表
                this.categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());
            }
        }
    }

    /*
     * 查询本节点下所包含的所有叶子节点，用于维护tb_category_brand中间表
     * @param category
     * @param leafNode
     */
    private void queryAllLeafNode(Category category,List<Category> leafNode){
        if(!category.getIsParent()){
            leafNode.add(category);
        }
        Example example = new Example(Category.class);
        example.createCriteria().andEqualTo("parentId",category.getId());
        List<Category> list = this.categoryMapper.selectByExample(example);

        for (Category category1:list){
            queryAllLeafNode(category1,leafNode);
        }
    }

    /*
     * 查询本节点下所有子节点
     * @param category
     * @param node
     */
    private void queryAllNode(Category category,List<Category> node){
        node.add(category);
        Example example = new Example(Category.class);
        example.createCriteria().andEqualTo("parentId",category.getId());
        List<Category> list = this.categoryMapper.selectByExample(example);

        for (Category category1:list){
            queryAllNode(category1,node);
        }
    }

    /**
     * 更新商品分类
     */
    public void updateCategory(Category category) {
        this.categoryMapper.updateByPrimaryKeySelective(category);
    }

    /**
     * 根据IDS查询商品分类
     */
    public List<Category> queryByIds(List<Long> ids) {
        final List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;
    }

    /**
     * 根据品牌ID查询商品分类信息
     */
    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }
}