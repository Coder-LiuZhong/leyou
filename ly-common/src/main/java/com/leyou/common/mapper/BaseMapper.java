package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;

/**
*  通用的BaseMapper
 *      继承了三个经常要用到的mapper，其他地方直接继承就可以调用
 *      类型不确定就泛型写个T
* */
@RegisterMapper     // 加上这个才能被扫描到
public interface BaseMapper<T> extends Mapper<T>, IdListMapper<T,Long>, InsertListMapper<T> {

    // tk.mybatis.mapper.common.special.InsertListMapper     这个有限制：实体的id只能叫ID，不能是其他什么skuId之类的要求
    // tk.mybatis.mapper.additional.insert.InsertListMapper  这个没限制
}
