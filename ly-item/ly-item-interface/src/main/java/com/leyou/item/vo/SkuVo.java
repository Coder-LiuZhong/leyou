package com.leyou.item.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 需要转换，正式开发中往往都需要有VO，视频教学里嫌麻烦就不搞VO了，这个文件只是个demo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuVo {

    private Long id;
    private String title;
    private Long price;
    private String image;
}
