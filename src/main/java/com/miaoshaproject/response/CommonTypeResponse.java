package com.miaoshaproject.response;

import lombok.Data;

@Data
public class CommonTypeResponse {
    private String status;
    private Object data;

    public CommonTypeResponse(Object data){
        this.status="success";
        this.data = data;
    }
    //success的时候 data是返回的数据
    //failed的时候，data是
    public CommonTypeResponse(String status, Object data){
        this.status=status;
        this.data = data;
    }
    public CommonTypeResponse(){

    }
}
