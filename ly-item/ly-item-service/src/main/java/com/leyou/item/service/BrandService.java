package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        // 分页
        PageHelper.startPage(page, rows);       // 当前页2，每页大小5；   形成sql：limit 5，5   第六条记录开始查询5条；   3和5过来就是limit 10,5 第十一个开始

        // 过滤  where name like "%x%" or letter == 'X' ORDER BY id DESC
        Example example = new Example(Brand.class);         // 利用反射到实体类里得到表名、主键等等。根据这种表进行sql操作
        if(StringUtils.isNoneBlank(key)){                   // 判断字符串是否是null、空
            // 过滤条件。新建一个条件。后面接上各种条件，或的关系就用or,一个参数是属性名，另一个参数是条件值
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }

        // 排序
        if(StringUtils.isNoneBlank(sortBy)){
            // 排序字句 ORDER BY 后面的内容
            String orderByClause = sortBy + (desc?" DESC":" ASC");
            example.setOrderByClause(orderByClause);
        }

        // 查询
        List<Brand> list = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        // 解析分页结果
        PageInfo<Brand> info = new PageInfo<>(list);           // 可进去看看源码；   控制台看到做了两次查询，一次统计总条数，一次是加上了过滤条件

        return new PageResult<>(info.getTotal(),list);
    }

    @Transactional
    public void savaBrand(Brand brand, List<Long> cids) {
        //brandMapper.insertSelective(brand);     // 两种新增，这种是有选择性的新增，判断brand里面有没有null字段，有就不新增这些字段，只新增那些非空的。
        int count = brandMapper.insert(brand);    // 1新增成功，0失败；     id 是自增长
        if (count!=1) {
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);      // 服务器新增品牌失败500
        }

        // 新增中间表
        for (Long cid : cids) {             // 快捷键生成cids.for
            count = brandMapper.insertCategoryBrand(cid,brand.getId());
            if (count != 1) {
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    public Brand queryById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> list = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

}
