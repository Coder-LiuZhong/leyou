package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor     //会生成一个包含所有变量的构造函数
@NoArgsConstructor      //生成一个无参数的构造方法
@Getter
public class LyException extends RuntimeException {

    private ExceptionEnum exceptionEnum;


}
