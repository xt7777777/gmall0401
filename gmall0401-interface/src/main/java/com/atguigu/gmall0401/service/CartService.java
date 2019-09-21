package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.CartInfo;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-20 19:25
 */
public interface CartService {

    /**
     * 添加一个购物车商品
     * @param userId
     * @param skuId
     * @param num
     * @return
     */
    public CartInfo addCart(String userId, String skuId, Integer num);

    /**
     * 根据用户id取该用户的购物车商品集合
     * @param userId
     * @return
     */
    List<CartInfo> cartList(String userId);

    /**
     * 合并购物车
     * @param userIdDest
     * @param userIdOrig
     * @return
     */
    List<CartInfo> mergeCartList(String userIdDest, String userIdOrig);
}
