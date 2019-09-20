package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.CartInfo;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-20 19:25
 */
public interface CartService {

    public CartInfo addCart(String userId, String skuId, Integer num);

    List<CartInfo> cartList(String userId);
}
