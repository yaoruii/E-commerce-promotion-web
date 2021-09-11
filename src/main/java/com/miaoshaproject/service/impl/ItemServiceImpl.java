package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    @Transactional//对好几个表同时操作，作为事务
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参：之前引入的validatorImpl -> validator
        ValidationResult validationResult = validator.validate(itemModel);
        if(validationResult.isHasErrors()){
            //有错误：
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validationResult.getMsg());
        }
        //转换itemmodel 为dataobject
        ItemDO itemDO = convertItemDOFromModel(itemModel);
        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        //插入到，itemDO的id就有了
        itemModel.setId(itemDO.getId());

        //接下来，是库存表：
        ItemStockDO itemStockDO = convertItemStockFromModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        //返回创建完成的对象：创建完成的对象要返回到上一层，让它知道创建完成的对象是什么样的状态：
        //getItemById()需要自己实现的一个service内部的函数
        //但我觉得这里，直接返回itemModel 不就好了。。。getItemById()更多的时候是用来查询某个商品的详情的吧
        return this.getItemById(itemModel.getId());
    }
    private ItemStockDO convertItemStockFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        BeanUtils.copyProperties(itemModel, itemStockDO);
        itemStockDO.setItemId(itemModel.getId());
        return itemStockDO;

    }
    private ItemDO convertItemDOFromModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        //将itemmodel内的字段值copy到itemdo内：
        BeanUtils.copyProperties(itemModel, itemDO);
        //price字段在itemmodel内是bigdecimal类型，所以需要手动赋值：
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    @Override
    public List<ItemModel> listItems() {
        //先实现简单版：查询出所有的list
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        //转化：
        List<ItemModel> itemModelList = new ArrayList<>();
        for(ItemDO itemDO: itemDOList){
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = convertFromDataObject(itemDO, itemStockDO);
            itemModelList.add(itemModel);
        }
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO==null){
            return null;
        }
        //通过item_id去查找库存表的记录：
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);
        //convert to model
        ItemModel itemModel = convertFromDataObject(itemDO, itemStockDO);

        //获取当前item的秒杀活动：
        PromoModel promoModel = promoService.getPromo(id);
        if(promoModel != null &&promoModel.getStatus()!=3){
            //只有现在和未来的活动被关联：
            //模型聚合：将秒杀商品和秒杀活动关联在一起：
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    private ItemModel convertFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO) {
        if(itemDO == null){
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
    @Override
    @Transactional//保证事务一致性：
    public boolean decreaseStock(Integer itemId, Integer amount){
        //比如itemid=6, 那么对item_stock这张表的item_id=6的那一行数据进行加锁，因为要对这行数据进行update stock这个字段值
        //int affectedRows = itemStockDOMapper.decreaseStock(itemId, amount);

        //new code
        //amount *-1 就是一个减操作了：result是操作后的value的值：
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId, amount.intValue()*-1);
        if(result>=0){
            //更新库存成功
            return true;
        }
        //更新库存失败
        else return false;
        //但是上述代码，没有将减后的库存同步到数据库，所以还要同步一下：

    }

    @Override
    @Transactional//保证事务一致性：
    public boolean increaseSales(Integer itemId, Integer amount){
        //比如itemid=6, 那么对item_stock这张表的item_id=6的那一行数据进行加锁，因为要对这行数据进行update stock这个字段值
        int affectedRows = itemDOMapper.increaseSales(itemId, amount);
        if(affectedRows>0){
            //更新库存成功
            return true;
        }
        //更新库存失败
        else return false;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id){
        ItemModel itemModel = (ItemModel)redisTemplate.opsForValue().get("item_validate_"+id);
        if(itemModel==null){
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_"+id, itemModel);
            redisTemplate.expire("item_validate_"+id, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }
}
