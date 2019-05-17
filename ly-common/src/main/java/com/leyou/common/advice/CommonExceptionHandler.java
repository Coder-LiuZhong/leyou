package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 通用异常处理工具
 *      ControllerAdvice注解：是针对Controller的。 默认情况下自动拦截所有的Controller。可以设置拦截其他，没必要去了解。
 *      ExceptionHandler注解：拦截指定异常，所以类里面可以写很多不同的异常拦截方法。所有异常都可以写
 * */
@ControllerAdvice
public class CommonExceptionHandler {

    // 拦截指定异常，处理返回给页面
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());  // 存在两个问题： 1. 状态码写死了; 2. 响应结果太单调
    }

    // 优化两个问题后，拦截指定自定义的异常
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleException(LyException e){
        ExceptionEnum exceptionEnum = e.getExceptionEnum();
        return ResponseEntity.status(exceptionEnum.getCode()).body(new ExceptionResult(e.getExceptionEnum()));
    }

}