package com.miaoshaproject.service.model;

import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PromoModel implements Serializable {
    private Integer id;

    //活动名称：
    private String promoName;
    //开始时间：
    private Date startTime;
    private Date endTime;

    private Integer itemId;

    private BigDecimal promoItemPrice;

    //依靠当前时间和开始结束时间做比较，去聚合出：当前秒杀活动的状态如何，并把它收集在promoodel中，便于外层系统直接调用，而不用外层的service再去判断时间
    private Integer status;


}
