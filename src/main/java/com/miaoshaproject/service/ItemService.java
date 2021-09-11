package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

/**
 * service里面操作的都是领域模型，也就是model，而不是dataobject
 */
public interface ItemService {
    //创建商品：
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表浏览：
    List<ItemModel> listItems();
    //商品详情浏览：
    ItemModel getItemById(Integer id);

    //落单减库存：
    boolean decreaseStock(Integer itemId, Integer amount);

    //增加销量：
    boolean increaseSales(Integer itemId, Integer amount);

    //验证item及promo是否有效：item 及promo缓存模型
    ItemModel getItemByIdInCache(Integer id);
}
