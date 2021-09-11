package com.miaoshaproject.error;

public interface CommonError {

    public int getErrorCode();
    public String getErrorMsg();
    public CommonError setMsg(String message);

}
