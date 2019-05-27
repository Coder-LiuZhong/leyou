package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据商品分类ID查询商品规格组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){      // @PathVariable要跟路径占位符{cid}一起用的
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }

    /**
     * 根据条件查询商品规格组内的商品子规格
     *      商品规格里从一条组信息进入子信息会用到，只传组ID
     *      商品新增选中商品分类之后会自动调用，参数不一样,只传商品分类ID
     *      searching为以后做准备
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否搜索
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamList(
            @RequestParam(value="gid", required = false)Long gid,       // required=false 不是必须传的参数
            @RequestParam(value="cid", required = false)Long cid,
            @RequestParam(value="searching", required = false)Boolean searching
    ){
        return ResponseEntity.ok(specificationService.queryParamList(gid, cid, searching));
    }

    /**
     * 根据分类查询规格组及组内参数
     * @param cid
     * @return
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryListByCid(cid));
    }
}
