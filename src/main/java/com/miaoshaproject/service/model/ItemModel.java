package com.miaoshaproject.service.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class ItemModel implements Serializable {

    private Integer id;
    //商品名称
    @NotBlank(message="商品名称不能为空")
    private String title;
    //商品价格
    @NotNull(message="价格不能为空")
    @Min(value=0, message="价格必须大于0")
    private BigDecimal price;
    //商品库存
    @NotNull(message="库存不能为空")
    private Integer stock;
    //商品描述
    @NotBlank(message="商品描述不能为空")
    private String description;
    //商品销量
    //销量字段非入参范围，不是传进来的，没有验证规则
    private Integer sales;
    //商品图片（URL格式链接）
    @NotBlank(message = "商品图片url不能为空")
    private String imgUrl;
    //当前item的还未结束的秒杀活动：正在进行或者还未开始的活动：
    private PromoModel promoModel;

    //以上是领域模型，在设计数据库表的时候，要根据具体情况，进行正确的拆分：
}
