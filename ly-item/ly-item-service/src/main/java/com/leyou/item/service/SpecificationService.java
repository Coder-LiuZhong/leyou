package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据商品分类ID查询商品规格组
     */
    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询条件
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        //开始查询
        List<SpecGroup> list = specGroupMapper.select(specGroup);       // 非空字段作为条件查询
        //判断
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);      //没查到
        }
        return list;
    }

    /**
     * 根据条件查询商品规格组内的商品子规格
     * @param gid 组id
     * @param cid 商品分类id
     * @param searching 是否搜索
     */
    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        //查询条件
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        //开始查询
        List<SpecParam> list = specParamMapper.select(specParam);       // 根据specParam的非空字段去查询
        //判断
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);      //没查到
        }
        return list;
    }

    public List<SpecGroup> queryListByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);

        //查询当前分类下的参数
        List<SpecParam> specParams = queryParamList(null, cid, null);

        //先把规格参数变成map，map的key是规格组的id，map的值是组下的所有规格参数
        HashMap<Long, List<SpecParam>> map = new HashMap<>();

        for (SpecParam param : specParams) {
            if(!map.containsKey(param.getGroupId())){
                //这个组id在map中不存在，新增一个list
                map.put(param.getGroupId(), new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }

        //填充param到group
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
