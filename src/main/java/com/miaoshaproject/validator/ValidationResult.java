package com.miaoshaproject.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    //检验结果是否有错误
    private boolean hasErrors = false;
    //存放错误信息的map：
    private Map<String, String> errorMsg = new HashMap<>();

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(Map<String, String> errorMsg) {
        this.errorMsg = errorMsg;
    }

    //实现通用方法：通过格式化字符串信息获取错误结果的msg方法：
    public String getMsg(){
        return StringUtils.join(this.errorMsg.values().toArray(),",");
    }
}
