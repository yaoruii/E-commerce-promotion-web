package com.miaoshaproject.error;

//包装器业务异常类实现
public class BusinessException extends Exception implements CommonError{

    private CommonError commonError;

    public CommonError getCommonError() {
        return commonError;
    }

    public void setCommonError(CommonError commonError) {
        this.commonError = commonError;
    }

    public BusinessException(CommonError commonError){
        this.commonError = commonError;
    }
    public BusinessException(CommonError commonError, String msg){
        //可改写msg
        super();//一定要记得第一行先调用super();
        this.commonError = commonError;
        this.commonError.setMsg(msg);
    }

    @Override
    public int getErrorCode() {
        return this.commonError.getErrorCode();
    }

    @Override
    public String getErrorMsg() {
        return this.commonError.getErrorMsg();
    }

    @Override
    public CommonError setMsg(String message) {
        this.commonError.setMsg(message);
        return this;
    }
}
