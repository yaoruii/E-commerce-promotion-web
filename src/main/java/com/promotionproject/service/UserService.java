package com.promotionproject.service;

import com.promotionproject.error.BusinessException;
import com.promotionproject.service.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;
    UserModel validateLogin(String telephone, String password) throws BusinessException;

    UserModel getUserByIdInCache(Integer id);
}
