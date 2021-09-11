package com.promotionproject.controller;

import com.promotionproject.error.BusinessException;
import com.promotionproject.error.EmBusinessError;
import com.promotionproject.response.CommonTypeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    /**
     * 该method不止user层面的controller，其他的模块也需要，因此，可以将其放在basecontroller中
     * 然后，剩下的业务controller可以继承这个basecontroller
     * @param request
     * @param ex
     * @return
     */
//    定义exceptionHandle,处理未被controller层吸收的exception
    //当我收到什么样的exception时，才会进入这个处理环节：
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(HttpServletRequest request, Exception ex){
        //补获到了异常，将异常处理为：commonTypeResponse 类型的带有status和data的数据格式
        //status为fail， 其中的data为之前定义的emBusinessError的表明errorcode的枚举实例
        // 因为，我们自定义的businessexception有CommonError类型的成员变量
//        CommonTypeResponse commonTypeResponse = new CommonTypeResponse();
//        commonTypeResponse.setStatus("fail");
        Map<String, Object> responseData = new HashMap<>();
        //将exception类型的ex先强转成自定义的businessException的
        //强转前要判断下能否转：instanceof
        if(ex instanceof BusinessException){
            //属于businessexception才有下边的所有操作：
            BusinessException businessException = (BusinessException) ex;
            /**
             * setData这里，如果传入的是ex，那么会把整个exception的内容都传进去
             * 如果传的是businessException.getCommonError()：
             * 会得到这样的结果：{"status":"fail","data":"USER_NOT_EXIST"}，只有一个name
             * 所以，要用一个hashmap把里面的code和message获取出来，这就很不优雅了
             * 网易应该是用的json
             */
            //commonTypeResponse.setData( businessException.getCommonError());
            responseData.put("errCode", businessException.getErrorCode());
            responseData.put("errMsg", businessException.getErrorMsg());
//            return new CommonTypeResponse("fail",responseData);
        }
        else{
            //如果不是我们手动throw出来的businessException：比如代码中没有对null判断，直接操作了
            //定义一个通用的未知错误：
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrorCode());
            responseData.put("errMsg", ex.getMessage());
        }
        return new CommonTypeResponse("fail",responseData);
        //返回的object会去寻找本地路径下的一些页面文件，要对这个method加上@ResponseBody注解, 返回的才是commonTypeResponse对应的json

    }

}
