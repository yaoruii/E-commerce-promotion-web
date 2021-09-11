package com.promotionproject.service.impl;

import com.promotionproject.dao.PromoDOMapper;
import com.promotionproject.dataobject.PromoDO;
import com.promotionproject.service.ItemService;
import com.promotionproject.service.PromoService;
import com.promotionproject.service.model.ItemModel;
import com.promotionproject.service.model.PromoModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public PromoModel getPromo(Integer itemId) {
        //获取商品的还未结束的秒杀信息，结束时间大于当前时间：
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        //dataobject ->> model:
        PromoModel promoModel = convertModelFromDO(promoDO);
        if(promoModel != null){
            Date now = new Date();
            if(promoModel.getStartTime().after(now)){
                //还未开始：
                promoModel.setStatus(1);
            }else if(promoModel.getEndTime().before(now)){
                promoModel.setStatus(3);
            }
            else{
                //正在进行：
                promoModel.setStatus(2);
            }
        }
        return promoModel;
    }

    /**
     * 谁去调这个方法呢？运营后台发布秒杀活动，但本项目不考虑
     * 可以在item controller层写一个接口，调用该方法，发布活动
     * @param promoId
     */
    @Override
    public void publishPromo(Integer promoId) {
        //通过id获取活动：
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO.getItemId() == null || promoDO.getItemId().intValue()==0){
            return;
        }
        //获得对应商品：
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
        //将库存同步到缓存中：
        redisTemplate.opsForValue().set("promo_item_stock_"+ itemModel.getId(),itemModel.getStock());
        redisTemplate.expire("promo_item_stock_"+ itemModel.getId(),10, TimeUnit.MINUTES);
    }

    private PromoModel convertModelFromDO(PromoDO promoDO) {
        if(promoDO==null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartTime(promoDO.getStartTime());
        promoModel.setEndTime(promoDO.getEndTime());
        return promoModel;

    }
}
