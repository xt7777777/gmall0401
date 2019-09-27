package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.OrderInfo;
import com.atguigu.gmall0401.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-22 15:21
 */
public interface OrderService {

    public String saveOrder(OrderInfo orderInfo);

    public String genToken(String userId);

    public Boolean verifyToken(String userId, String token);

    public void delToken(String userId);

    public OrderInfo getorderInfo(String orderId);

    public void updateStatus(String orderId, ProcessStatus processStatus, OrderInfo... orderInfos);

    List<Integer> checkExpiredCoupon();

    void handleExpiredCoupon(Integer couponId);

    List<Map> orderSplit(String orderId, String wareSkuMap);

    public Map initWareParamJson(String orderId);
}
