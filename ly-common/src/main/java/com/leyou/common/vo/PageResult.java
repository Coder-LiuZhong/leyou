package com.leyou.common.vo;

import lombok.Data;

import java.util.List;

/**
 * view object 专门给页面用的对象
 * @Author: cuzz
 * @Date: 2018/11/2 9:55
 * @Description: 分页类
 */
@Data
public class PageResult<T> {
    private Long total;     // 总条数
    private Integer totalPage; // 总页数
    private List<T> items;  // 当前页数据

    public PageResult() {
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageResult(Long total, int totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }

}
