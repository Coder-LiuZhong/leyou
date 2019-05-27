package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点ID查询商品分类
     *      返回值不用List<Category>，采用REST风格
     * */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryListByPid(@RequestParam("pid") Long pid) {
        //return ResponseEntity.status(HttpStatus.CREATED).body(null);
        return ResponseEntity.ok( categoryService.queryCategoryListByPid(pid) );     // 简写
    }

    /**
     * 保存
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveCategory(Category category){
        categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除
     * @return
     */
    @DeleteMapping("cid/{cid}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("cid") Long id){
        this.categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 更新
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateCategory(Category category){
        categoryService.updateCategory(category);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * 根据品牌ID查询商品分类信息
     *      用于修改品牌页面，品牌对应的商品分类信息回显
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid){
        return ResponseEntity.ok(categoryService.queryByBrandId(bid));
    }
}
