package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

/**
 * 自定义异常结果信息返回
 *      根据自己的实际情况定义各种属性。
 * */
@Data
public class ExceptionResult {

    private int status;
    private String message;
    private Long timestamp;

    // 构造方法
    public ExceptionResult(ExceptionEnum em) {
        this.status = em.getCode();
        this.message = em.getMsg();
        this.timestamp = System.currentTimeMillis();
    }
}
