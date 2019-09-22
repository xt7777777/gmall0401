package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.OrderInfo;

/**
 * @author xtsky
 * @create 2019-09-22 15:21
 */
public interface OrderService {

    public void saveOrder(OrderInfo orderInfo);

    public String genToken(String userId);

    public Boolean verifyToken(String userId, String token);

    public void delToken(String userId);

}
