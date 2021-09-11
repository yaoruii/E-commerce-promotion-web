package com.promotionproject.controller;

import com.promotionproject.error.BusinessException;
import com.promotionproject.error.EmBusinessError;
import com.promotionproject.response.CommonTypeResponse;
import com.promotionproject.service.OrderService;
import com.promotionproject.service.model.OrderModel;
import com.promotionproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true",origins = {"*"})
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;

    //封装下单请求：
    //修改：添加参数：promoId
    @RequestMapping(value= "/createorder", method={RequestMethod.POST}, consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonTypeResponse createOrder(@RequestParam(name="itemId") Integer itemId,
                                          @RequestParam(name="amount") Integer amount,
                                          @RequestParam(name="promoId", required = false) Integer promoId) throws BusinessException {

        //useid需要获取登陆信息：
        //在登陆模块，当用户登陆成功后，进行了如下操作，所以，当前登陆用户的信息我们是保存在了httpservletrequest.session中的
        /* 以前的方法
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
         */
        //返回的是object，所以用Boolean类型，装箱

        /* 基于cookie的sessionid的方式验证登陆与否：
        Boolean isLogin =(Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        */
        //修改：
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            //没登陆
            throw new BusinessException(EmBusinessError.NOT_LOGIN);
        }
        /*
        **获取用户信息：
        */
        /* 原来的方式：
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        */
        //新的方式：
        //token存在：
        //之前在login的时候，把token 和 usrmodel 对象实例 存在了redistemplate中了，get一下：
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null){
            //存在uuid的key，value为null
            //说明：会话过期了
            //没登陆
            throw new BusinessException(EmBusinessError.NOT_LOGIN);
        }



        //调用service下单：
        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId, promoId,amount);



        //创建类，不需要展示给前端什么，所以这里直接传null:
        return new CommonTypeResponse(null);

    }


}
