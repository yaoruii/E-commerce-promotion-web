package com.promotionproject.error;

public enum EmBusinessError implements CommonError{
    //在这里定义几个常见的错误码：code不能用0开头，因为会把前缀0都去掉，定义为：1000x这样
    //通用错误类型：
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),

    //20000开头的错误码对应用户信息相关错误
    USER_NOT_EXIST(20001, "用户不存在"),
    LOGIN_FAILED(20002,"用户名或密码有误"),
    NOT_LOGIN(20003,"请先登陆"),

    //30000开头的：订单相关
    STOCK_NOT_ENOUGH(30001, "库存不足");

    ;

    private int errCode;
    private String errMsg;

    //构造器：

    /**
     * 枚举被设计成是单例模式，即枚举类型会由JVM在加载的时候，实例化枚举对象，
     * 你在枚举类中定义了多少个就会实例化多少个，JVM为了保证每一个枚举类元素的唯一实例，
     * 是不会允许外部进行new的，所以会把构造函数设计成private，防止用户生成实例，破坏唯一性。
     */
    private EmBusinessError(int code, String msg){
        errCode = code;
        errMsg = msg;
    }
    @Override
    public int getErrorCode() {
        return this.errCode;
    }

    @Override
    public String getErrorMsg() {
        return this.errMsg;
    }

    /**
     * 对通用错误码进行修改，定制更加具体的错误码，比如参数不合法错误码中指定具体参数
     * 实例只有一个，但可以修改
     * @param message
     * @return
     */
    @Override
    public CommonError setMsg(String message) {
        this.errMsg = message;
        return this;
    }
}
