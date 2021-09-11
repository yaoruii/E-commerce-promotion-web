package com.miaoshaproject.controller.viewObject;

import lombok.Data;
import org.joda.time.DateTime;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
@Data
public class ItemVO {
    private Integer id;
    //商品名称
    private String title;
    //商品价格
    private BigDecimal price;
    //商品库存
    private Integer stock;
    //商品描述
    private String description;
    //商品销量
    //销量字段非入参范围，不是传进来的，没有验证规则
    private Integer sales;
    //商品图片（URL格式链接）
    private String imgUrl;

    //展示商品的时候需要展示：秒杀活动的一些信息：0为默认值：无秒杀活动，1，还未开始，2，正在进行中
    private int promoStatus;

    private BigDecimal promoPrice;

    private Integer promoId;

    private String startTime;//倒计时展示

}
