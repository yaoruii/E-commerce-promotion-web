package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceInfoDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceInfoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceInfoDOMapper sequenceInfoDOMapper;

    @Autowired
    private OrderService orderService;//自身

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        //1，校验下单状态：
        //1，商品是否存在，2，用户是否合法，3，购买数量是否正确,4,校验活动信息
        //ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        //UserModel userModel = userService.getUserById(userId);
        UserModel userModel = userService.getUserByIdInCache(userId);
        if(userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }
        //购买数量：0-99
        if(amount<=0 || amount>99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"购买数量不合法");
        }
        //校验活动信息
        if(promoId!=null){
            //有秒杀
            //（1）：活动和商品是否对应的上：itemModel里面有个promomodel的属性，里面有个id，看两者是否相等
            //这一步：itemService.getItemById(itemId)，帮我们做好了，将item和promo绑定，随时用随时get，而不用再查询了
            if(promoId != itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }
            //（2）：活动是否正在进行中：itemModel里面有个promomodel的属性，里面有个status
            if(itemModel.getPromoModel().getStatus()!=2){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动还未开始");
            }
            //promoid合法，为当前商品的秒杀活动
        }//promoid为null，平销商品

        //2，落单减库存：创建订单的时候锁定库存
        //调用itemservice的减库存操作：
        if(!itemService.decreaseStock(itemId, amount)){
            //返回的是false: 受影响行数为0，所以是库存不足：
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3，订单入库
        //创建ordermodel:
        OrderModel orderModel = new OrderModel();
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        orderModel.setUserId(userId);
        //单价要分情况而定：
        if(promoId==null){
            orderModel.setItemPrice(itemModel.getPrice());
        }
        else{
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }

        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易订单号（流水号）：不是自增的，有一定的规则要求：
        orderModel.setId(orderService.generateOrderNo());
        //将orderModel转换为orderDO：
        OrderDO orderDO = convertDOFromOrderModel(orderModel);
        //入库：
        orderDOMapper.insertSelective(orderDO);
        //增加item的销量：
        itemService.increaseSales(itemId, amount);


        //4，返回前端
        return orderModel;
    }
//    //测试下生成id的这个函数对不对呀：
//    public static void main(String[] args){
//        //获取当前的日期时间
//        LocalDateTime now = LocalDateTime.now();
//        //格式化format一下：
//        String date= now.format(DateTimeFormatter.BASIC_ISO_DATE);
//        System.out.print(date);
//    }
    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public String generateOrderNo(){
        //订单号有16位：
        StringBuilder sb = new StringBuilder();
        // 前8位时间信息：年月日，20210215，让订单在时间纬度上有一个可以归档切分的参考值，比如，可以将以前的数据归档掉，为了承载新的数据
        // 中间6位为自增序号，999999，：一天的最大订单数
        // 最后两位为分库分表位：00～99, 比如：将订单拆分到100个库中的100个表里面，分库规则比如：userId%100, 分散数据库的查询和落单压力。
        //获取当前的日期时间
        LocalDateTime now = LocalDateTime.now();
        //格式化format一下：
        sb.append(now.format(DateTimeFormatter.BASIC_ISO_DATE));
        //自增序列：
        //获取当前业务的sequence: order_info
        SequenceInfoDO sequenceInfoDO = sequenceInfoDOMapper.getSequenceByName("order_info");
        int sequence = sequenceInfoDO.getCurrentValue();
        //更新新的current_value值：
        sequenceInfoDO.setCurrentValue(sequence+sequenceInfoDO.getStep());
        //更新后的值入库：
        sequenceInfoDOMapper.updateByPrimaryKeySelective(sequenceInfoDO);
        String sequenceStr = String.valueOf(sequence);
        //看当前的sequencestr作为一个字符串，距离我们需要的6位还缺几位，缺的位置补前缀0：
        for(int i=0; i< 6-sequenceStr.length();i++){
            sb.append("0");
        }
        sb.append(sequenceStr);

        //最后2位的分库分表位：暂时写死：
        sb.append("00");

        return sb.toString();
        //问题一：sequence 的current_value没有设置最大值，可能最后超过了6位：
        //解决：在表里面设置一个最大值字段。当加上step后的new current_value大于最大值时，就set为initial value
        //问题二：


    }
    private OrderDO convertDOFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        //itemPrice 和orderPrice这里要改一下。。。草
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;

    }
}
