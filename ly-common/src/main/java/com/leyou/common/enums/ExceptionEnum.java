package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  异常消息枚举
 *      只提供getXX方法
 *
 *  枚举
 *      一般的类可以有无数个实例个数,枚举是只有固定实例个数的类。
 *      实例都是提前创建好的。
 *      枚举里的构造函数默认是私有的
 * */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {

    //private static final ExceptionEnums ff = new ExceptionEnums(1, "价格不能为空");
    PRICE_CANNOT_BE_NULL(400,"价格不能为空！"),     // 这就是一个对象；  简写，等同于上面那句的意义。  有多个就逗号隔开，最后一定是个分号结尾。  并且要放到类的最前面。
    GOODS_ID_CANNOT_BE_NULL(400,"商品ID不能为空"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    CATEGORY_NOT_FOND(404,"商品分类没查到"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数不存在"),
    SPU_DETAIL_NOT_FOUND(404,"商品详情不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    BRAND_UPDATE_ERROR(500,"修改品牌失败"),
    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    GOODS_UPDATE_ERROR(500,"修改商品失败")

    ;

    private int code;
    private String msg;
}
