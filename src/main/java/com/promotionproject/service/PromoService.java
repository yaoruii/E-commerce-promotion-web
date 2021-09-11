package com.promotionproject.service;

import com.promotionproject.service.model.PromoModel;

public interface PromoService {
    //根据商品的itemid，返回即将到来的该商品对应的秒杀活动：
    PromoModel getPromo(Integer itemId);

    void publishPromo(Integer promoId);

}
