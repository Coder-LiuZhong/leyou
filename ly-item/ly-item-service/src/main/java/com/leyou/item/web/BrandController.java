package com.leyou.item.web;


import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 分页查询品牌
     *      先分析页面需要什么
     *      测试：http://api.leyou.com/api/item/brand/page
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,        // 当前页
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,        // 每页显示
            @RequestParam(value = "sortBy", required = false) String sortBy,       // 排序的字段
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,    // 是否降序
            @RequestParam(value = "key", required = false) String key) {           // 搜索的关键字
        return ResponseEntity.ok( brandService.queryBrandByPage(page, rows, sortBy, desc, key) );
    }

    /**
     * 新增品牌
     * @param brand     新增的时候填写的品牌信息
     * @param cids      新增的时候选好的这个品牌下面会有哪些商品分类
     */
    @PostMapping
    public ResponseEntity<Void> savaBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        brandService.savaBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();    // 新增成功201CREATED;  有返回值就写body，没有就写build
    }

    /**
     * 修改品牌
     * @param brand     新增的时候填写的品牌信息
     * @param cids      新增的时候选好的这个品牌下面会有哪些商品分类
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        brandService.updateBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * 删除品牌
     *      删除tb_brand中的数据,单个删除、多个删除二合一
     *      删除多个，前台多选的时候传过来就是用-连接
     * @param bid
     * @return
     */
    @DeleteMapping("bid/{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") String bid){
        String separator = "-";
        if(bid.contains(separator)){
            String[] ids = bid.split(separator);
            for (String id:ids){
                this.brandService.deleteBrand(Long.parseLong(id));
            }
        }else {
            this.brandService.deleteBrand(Long.parseLong(bid));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 根据商品分类ID查询品牌
     *      新增商品页面，选择商品分类后，对应的品牌下拉框需要显示
     * @param cid
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCategory(@PathVariable("cid") Long cid) {
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    /**
     * 根据ID查询品牌
     *      全文搜索用到  http://localhost:8081/brand/1
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(brandService.queryById(id));
    }

    /**
     * 根据IDs列表查询品牌列表
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }

}
