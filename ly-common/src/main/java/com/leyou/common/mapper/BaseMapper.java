package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;

@RegisterMapper     // 加上这个才能被扫描到
public interface BaseMapper<T> extends Mapper<T>, IdListMapper<T,Long>, InsertListMapper<T> {

    // tk.mybatis.mapper.common.special.InsertListMapper     这个有限制：实体的id只能叫ID，不能是其他什么skuId之类的要求
    // tk.mybatis.mapper.additional.insert.InsertListMapper  这个没限制
}
