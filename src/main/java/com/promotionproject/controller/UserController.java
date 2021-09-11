package com.promotionproject.controller;


//import com.alibaba.druid.util.StringUtils;
import com.promotionproject.controller.viewObject.UserVO;
import com.promotionproject.error.BusinessException;
import com.promotionproject.error.EmBusinessError;
import com.promotionproject.response.CommonTypeResponse;
import com.promotionproject.service.UserService;
import com.promotionproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Controller
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     *
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/login",method={RequestMethod.POST}, consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonTypeResponse login(@RequestParam(name="telephone") String telephone,
                                    @RequestParam(name="password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验：不能为空
        if(StringUtils.isEmpty(telephone) || StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"参数不能为空");
        }
        //用户登陆是否合法：
        //加密password
        UserModel userModel = userService.validateLogin(telephone, this.encodeByMd5(password));
        /*
        //将"登陆凭证"加入到用户登陆成功的session内：（此处是单点登陆）
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
        */

        //修改成：若用户登陆成功后，将对应的登陆信息和登陆凭证一起存入redis中
        //生成登陆凭证token：UUID，保证唯一性（会话全局唯一性）
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replaceAll("-","");
        //建立token和用户登陆态的联系：只要redis中存在这个uuid的key，这个用户的登陆台就认为存在
        redisTemplate.opsForValue().set(uuidToken, userModel);
        //设置redis缓存的失效时间为1小时：
        redisTemplate.expire(uuidToken, 1, TimeUnit.HOURS);


        //uuid返回给客户端
        return new CommonTypeResponse(uuidToken);
    }

    /**
     * 用户注册:
     * @CrossOrigin: 允许跨域传输所有header参数，将token放入到header域做session共享的跨域传输
     * @return
     */
    @RequestMapping(value = "/register",method={RequestMethod.POST}, consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonTypeResponse register(@RequestParam(name="telephone") String telephone,
                                       @RequestParam(name="name") String name,
                                       @RequestParam(name="gender")Integer gender,
                                       @RequestParam(name="age") Integer age,
                                       @RequestParam(name="otpcode") String otpcode,
                                       @RequestParam(name="password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和otpcode一一对应：之前放在了session中了
        String insessionOtpCode = (String) httpServletRequest.getSession().getAttribute(telephone);
        if(!StringUtils.equals(otpcode,insessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码错误");
        }
        //合法验证码：注册流程：
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender)));
        userModel.setTelephone(telephone);
        userModel.setRegisterMode("byPhone");//肯定不能这样处理的。。
        //密码要加密处理一下：string的getByte()方法可以将字符串转换成byte数组
        userModel.setEncrptPassWord(this.encodeByMd5(password));
        userService.register(userModel);
        return new CommonTypeResponse(null);

    }
    /**
     * 加密
     */
    private String encodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方式：
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Base64.Encoder encoder = Base64.getEncoder();
        //加密字符串：
        String newStr = encoder.encodeToString(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }    /**
     * 用户获取otp短信接口
     * @param ：telephone
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/getOtp",method={RequestMethod.POST}, consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonTypeResponse getOtp(@RequestParam(name="telephone")String telephone){
        //以一定规则生成验证码：5位数字
        Random random = new Random();
        int randomInt = random.nextInt(9999);
        //如果生成的是0-99999的数字，那么就无法满足验证码5位的要求,可以将randomint加10000
        randomInt += 10000;//10000~19999
        String otpCode = String.valueOf(randomInt);

        //将该code同用户的手机号相关联：
        //方法很多，比如使用redis就非常合适，但这里先不涉及
        //这里采用httpsession的方式绑定手机号和otpcode
        //如何接入：拿到httpservletrequest对象，即可拿到session，通过bean的方式注入进来
        //bean注入：单例模式，那么为何支持多个用户的并发访问呢？
        httpServletRequest.getSession().setAttribute(telephone, otpCode);
        
        //3: 将otpcode发送给用户，略
        System.out.print("手机号是："+ telephone + "\n 验证码是：" + otpCode);
        return new CommonTypeResponse(null);
    }


    @RequestMapping("/get")
    @ResponseBody
    public CommonTypeResponse getUserById(@RequestParam("id") Integer id) throws BusinessException {
        /**
        UserModel userModel = userService.getUserById(id);
        return userModel;
         */
        //前端并不需要所有的UserModel的属性，所以可以再建一个userVO的类，只保留展示的属性，这肯定不是实际场景下的用法
        //我们可以select的时候就只选择需要的属性，用jsonobject返回
        //但这里先和他一致吧，后期如果没改进自己再尝试下
        UserModel userModel = userService.getUserById(id);
        //假设用户不存在：
        if(userModel == null){
            //异常抛出去了，到了tomcat的容器层，前端报white page，500，接下来要处理下，避免这样的返回结果
            //处理方法：定义exceptionHandle
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserVO userVO = convertFromUserModel(userModel);
        //返回通用对象
        return new CommonTypeResponse(userVO);

    }
    private UserVO convertFromUserModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }



}
