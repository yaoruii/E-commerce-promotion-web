package com.promotionproject.service;

import com.promotionproject.error.BusinessException;
import com.promotionproject.service.model.OrderModel;

public interface OrderService {

    //创建订单：
    //修改：传一个promoId, 根据这个promoid去检验是否属于对应的商品，且活动已经开始：
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;

    public String generateOrderNo();

    //
}
