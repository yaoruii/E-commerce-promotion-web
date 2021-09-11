package com.miaoshaproject.service.model;


import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * data object 文件夹下的userDO和UserPasswordDO是和数据库表一一对应的
 * 但却不是我们要操作的对象，比如真正的用户应该是拥有password属性的，但是password在数据库设计中一般是存放在另一个表中的（加密什么的？
 * 所以，springMVC中业务逻辑交互的模型是：usermodel，拥有userDO的所有字段和password字段
 * 数据模型层是两个，领域模型对象是一个
 */
public class UserModel implements Serializable {
    private Integer id;
    @NotBlank(message="用户名不能为空")
    private String name;
    @NotNull(message="性别不能不填")
    private Byte gender;
    @NotNull(message="年龄不能不填")
    @Min(value =0, message="年龄必须大于0")
    @Max(value=150, message="年龄不能超过150岁")
    private Integer age;
    @NotBlank(message="手机号不能为空")
    private String telephone;
    private String registerMode;
    private String thirdPartyId;
    //比userDO多了这个字段，这才是逻辑交互的model
    @NotBlank(message="密码不能为空")
    private String encrptPassWord;

    public String getEncrptPassWord() {
        return encrptPassWord;
    }

    public void setEncrptPassWord(String encrptPassWord) {
        this.encrptPassWord = encrptPassWord;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }
}
