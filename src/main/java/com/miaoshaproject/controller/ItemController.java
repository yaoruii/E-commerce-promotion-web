package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewObject.ItemVO;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonTypeResponse;
import com.miaoshaproject.service.CacheService;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PromoService promoService;

    //创建商品：
    @RequestMapping(path= "/create",method={RequestMethod.POST}, consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonTypeResponse createItem(@RequestParam(name="title") String title,
                                         @RequestParam(name="description") String description,
                                         @RequestParam(name="price")BigDecimal price,
                                         @RequestParam(name="stock") Integer stock,
                                         @RequestParam(name="imgUrl") String imgUrl) throws BusinessException {
        //封装service请求用来创建商品：
        //先new一个itemmodel，然后把这个对象传给service层，去入库等：
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel returnModel = itemService.createItem(itemModel);
        ItemVO itemvo = convertItemVOFromModel(returnModel);
        return new CommonTypeResponse(itemvo);

    }
    private ItemVO convertItemVOFromModel(ItemModel returnModel) {
        if(returnModel==null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(returnModel, itemVO);
        //加上秒杀信息：
        if(returnModel.getPromoModel()!=null){
            //有正在进行或即将进行的秒杀活动：
            PromoModel promoModel = returnModel.getPromoModel();
            itemVO.setPromoStatus(promoModel.getStatus());
            itemVO.setPromoId(promoModel.getId());
            itemVO.setStartTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(promoModel.getStartTime()));
            itemVO.setPromoPrice(promoModel.getPromoItemPrice());
        }
        return itemVO;
    }
    //商品页表浏览
    @RequestMapping(path="/list",method={RequestMethod.GET})
    @ResponseBody
    public CommonTypeResponse listItems(){
        List<ItemModel> itemModelList  = itemService.listItems();
        //转换：
        List<ItemVO> itemVOList = new ArrayList<>();
        for(ItemModel itemModel: itemModelList){
            itemVOList.add(convertItemVOFromModel(itemModel));
        }
        return new CommonTypeResponse(itemVOList);
    }


    //商品详情页浏览：
    @RequestMapping(path="/getItemDentails",method={RequestMethod.GET})
    @ResponseBody
    public CommonTypeResponse getItemDetails(@RequestParam(name="id") Integer id){
        ItemModel itemModel = null;
        //先取本地缓存：
        itemModel = (ItemModel) cacheService.getCommonCache("item_"+id);//key的方式和redis的一样
        if(itemModel==null){
            //本地缓存不存在：从redis中取
            //itemmodel的key的设计方式：以item开头 + itemid
            itemModel = (ItemModel)redisTemplate.opsForValue().get("item_"+id);
            //redis中不存在：访问下游的service
            if(itemModel == null){
                itemModel = itemService.getItemById(id);//之前实现好的
                //设置itemmodel到redis中：
                redisTemplate.opsForValue().set("item_"+id,itemModel);//item model要implements serializable
                //10分钟后失效：
                redisTemplate.expire("item_"+id,10, TimeUnit.MINUTES);
            }
            //将数据存入本地缓存：
            cacheService.setCommonCache("item_"+id, itemModel);
        }

        ItemVO itemVO = convertItemVOFromModel(itemModel);
        return new CommonTypeResponse(itemVO);
    }

    //发布该商品的秒杀活动：将库存缓存
    @RequestMapping(path="/publishpromo",method={RequestMethod.GET})
    @ResponseBody
    public CommonTypeResponse publishPromo(@RequestParam(name="id") Integer id){
        promoService.publishPromo(id);
        return new CommonTypeResponse(null);

    }
}
