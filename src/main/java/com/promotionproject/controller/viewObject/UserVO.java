package com.promotionproject.controller.viewObject;

import lombok.Data;

@Data
public class UserVO {
    private Integer id;
    private String name;
    private Byte gender;
    private Integer age;
    private String telephone;
}
