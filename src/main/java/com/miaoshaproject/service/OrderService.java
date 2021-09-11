package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;
import com.sun.tools.corba.se.idl.constExpr.Or;

public interface OrderService {

    //创建订单：
    //修改：传一个promoId, 根据这个promoid去检验是否属于对应的商品，且活动已经开始：
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;

    public String generateOrderNo();

    //
}
