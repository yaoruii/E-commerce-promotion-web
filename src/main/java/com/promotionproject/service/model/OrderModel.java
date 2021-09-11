package com.promotionproject.service.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户下单的交易模型
 */
@Data
public class OrderModel {
    //为什么使用string 来作为id：因为一般情况下，id是具有明确属性的，比如：201810210001234，前边那部分是当天的日期
    private String id;
    //购买人
    private Integer userId;
    //商品id
    private Integer itemId;
    //购买数量
    private Integer amount;
    //商品的价格是不断变化的，今天10，明天可能12元，所以，这里冗余一个字段，表示该商品下单时的单价：（日后数据库里的价格可能变了
    //且：若promoId 非空，则表示：秒杀价格
    private BigDecimal itemPrice;
    //购买金额
    //同理：若promoId 非空，则表示：秒杀价格
    private BigDecimal orderPrice;

    private Integer promoId;



}
