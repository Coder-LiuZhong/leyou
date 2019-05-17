package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_spec_param")
@Data
public class SpecParam {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    @Column(name="`numeric`")       // 加了两个点，Esc键下面的那个点哦；  一个专业的sql语句， 里面的每个字段都加了两点，避免产生歧义，因为sql中numeric是个关键字
    private Boolean numeric;        // 通用mapper通过这个实体类会帮我们自动生成sql，@Column作用是指定生成sql的时候这个字段在生成sql时候的名称
    private String unit;
    private Boolean generic;
    private Boolean searching;
    private String segments;

}
