package com.leyou.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 *  读取yml里面的配置属性
 *  在需要用到这些属性的类上@EnableConfigurationProperties(UploadProperties.class)即可使用
 * */
@Data
@ConfigurationProperties(prefix="ly.upload")
public class UploadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}
