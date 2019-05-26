package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_brand")
public class Brand {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;            // KeySql主键策略，useGeneratedKeys = true 主键自增之后可以使用到主键

    private String name;        // 品牌名称

    private String image;       // 品牌图片

    private String letter;      // 品牌的首字母
}