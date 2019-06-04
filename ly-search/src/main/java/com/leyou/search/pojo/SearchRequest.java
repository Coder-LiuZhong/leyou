package com.leyou.search.pojo;

import java.util.Map;

/**
 * 专门用来接收搜索的请求参数
 * 因为页面传过来的是JSON的对象字符串，后台接收只能是对象来接收
 * */
public class SearchRequest {
    private String key;//搜索字段

    private Integer page;//当前页

    private Map<String, String> filter;

    private static final int DEFAULT_SIZE = 20;     //每页大小，不从页面接收，而是固定大小。而且万一用户查一百万呢？那数据库不挂了
    private static final int DEFAULT_PAGE = 1;      //默认页，有可能第一次页面就没传page，所以要有个默认值

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if(page == null){
            return DEFAULT_PAGE;
        }
        // 获取页码时做一些校验，不能小于1
        return Math.max(DEFAULT_PAGE, page);    // 返回最大值，传负数就返回1咯，毕竟比1小
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return DEFAULT_SIZE;
    }

    public Map<String, String> getFilter(){
        return filter;
    }
}
