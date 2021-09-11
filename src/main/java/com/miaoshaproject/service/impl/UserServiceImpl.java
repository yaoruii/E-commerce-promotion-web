package com.miaoshaproject.service.impl;


import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonTypeResponse;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validatorImpl;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public UserModel getUserById(Integer id){
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO==null){
            return null;
        }
        //通过用户id获取用户密码：
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);
        return convertFromDO(userDO, userPasswordDO);
    }

    @Override
    @Transactional//要对数据库的两个表进行两步操作，如果其中任意一个失败都将出错，所以，用transient注解标明为事务
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//                || userModel.getAge() ==null
//                || userModel.getGender() ==null
//                || StringUtils.isEmpty(userModel.getTelephone())){
//            //全都必添：在requestparam那里true一下不就好了。。。
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }
        //在要校验的model上添加对应的注解即可：
        ValidationResult validateResult = validatorImpl.validate(userModel);
        if(validateResult.isHasErrors()){
            //有错误：
            //将result中的message的map进行序列化后的string作为exception中的commonerror的message返回：
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,validateResult.getMsg());
        }
        //实现userModel --> userdataobject 的转换：
        UserDO userDO = convertFromModel(userModel);
        //将userDO这个对象插入到数据库，使用selective方法：
        try{
            userDOMapper.insertSelective(userDO);
        }catch(DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "该手机号已经被注册");
        }


        userModel.setId(userDO.getId());

        //实现userModel --> passworddataobject 的转换：
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
        return;


    }

    @Override
    public UserModel validateLogin(String telephone, String password) throws BusinessException {
        //通过手机获取用户信息：
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO==null){
            //手机号还未注册：
            throw new BusinessException(EmBusinessError.LOGIN_FAILED);
        }
        //根据user_id再去获取密码：
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDO(userDO, userPasswordDO);

        //这里传进来的password就是加密后的，再controller层对用户传的密码加密，再传过来：
        if(!StringUtils.equals(password, userModel.getEncrptPassWord())){
            throw new BusinessException(EmBusinessError.LOGIN_FAILED);
        }

        return userModel;



        //检验密码是否匹配：

    }

    public UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassWord());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;

    }
    public UserDO convertFromModel(UserModel userModel){
        //即使我们都知道能走到这一步，usemodel已经是not null，但还是要在这里判断下null，为了健壮性，可能其他method也会调用该convert
        if(userModel==null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }

    public UserModel convertFromDO(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();//系统会自动帮我们生成一个无参构造器，因为没有显式地定义任何构造器
        BeanUtils.copyProperties(userDO, userModel);
        //判断userPasswordDO是否为空：
        if(userPasswordDO !=null){
            userModel.setEncrptPassWord(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
    public UserModel getUserByIdInCache(Integer id){
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_"+id);
        if(userModel==null){
            userModel = this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_"+id, userModel);
            redisTemplate.expire("user_validate_"+id, 10, TimeUnit.MINUTES);
        }
        return userModel;
    }

}
