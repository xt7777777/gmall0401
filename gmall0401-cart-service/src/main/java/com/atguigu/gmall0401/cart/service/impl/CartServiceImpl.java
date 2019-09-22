package com.atguigu.gmall0401.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.CartInfo;
import com.atguigu.gmall0401.bean.SkuInfo;
import com.atguigu.gmall0401.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0401.service.CartService;
import com.atguigu.gmall0401.service.ManageService;
import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @author xtsky
 * @create 2019-09-20 19:27
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;



    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) {

        // 加数据库
        // 尝试取出已有数据 如果有 把数量更新 如果没有就添加
        CartInfo cartInfoQuery = new CartInfo();
        cartInfoQuery.setSkuId(skuId);
        cartInfoQuery.setUserId(userId);
        CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        if (cartInfoExists != null){

            cartInfoExists.setSkuName(skuInfo.getSkuName());
            //cartInfoExists.setCartPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum() + num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoExists.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);

        } else {

            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExists = cartInfo;

        }

        Jedis jedis = redisUtil.getJedis();
        // 加缓存 type key value
        String cartKey = "cart:" + userId + ":info";
        String cartInfoJson = JSON.toJSONString(cartInfoExists);
        jedis.hset(cartKey, skuId, cartInfoJson); // 新增 也可以覆盖

        jedis.close();

        return cartInfoExists;

    }

    @Override
    public List<CartInfo> cartList(String userId) {

        // 先查缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:" + userId + ":info";
        List<String> cartJsonList = jedis.hvals(cartKey);
        List<CartInfo> cartList = new ArrayList<>();
        if (cartJsonList != null && cartJsonList.size() > 0){ // 缓存命中
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartList.add(cartInfo);
            }

            cartList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());
                }
            });

            return cartList;

        } else {
            // 缓存未命中   缓存没有查数据库
            return loadCartCache(userId);
        }

    }



    /**
     *  缓存没有查数据库 同时加载到缓存中
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId) {

        // 读取数据库
        List<CartInfo> cartInfos = cartInfoMapper.selectCartListWithSkuPrice(userId);
        if (cartInfos != null && cartInfos.size() > 0) {
            // 加载到缓存中
            // 为了方便插入redis 把list -> map
            Map<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }

            Jedis jedis = redisUtil.getJedis();
            String cartKey = "cart:" + userId + ":info";
            jedis.del(cartKey);
            jedis.hmset(cartKey, cartMap);
            jedis.expire(cartKey, 60 * 60 * 24);
            jedis.close();
        }
        return cartInfos;

    }

    /**
     * 合并购物车
     * @param userIdDest
     * @param userIdOrig
     * @return
     */
    @Override
    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig) {

        // 先做合并
        cartInfoMapper.mergeCartList(userIdDest,userIdOrig);

        // 删除 合并后把临时购物车删除

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userIdOrig);
        cartInfoMapper.delete(cartInfo);

        //重新读取数据 加载缓存
        List<CartInfo> cartInfoList = loadCartCache(userIdDest);

        return cartInfoList;
    }



    /**
     * 如果cartKey不存在的话  加载缓存
     * @param userId
     */
    public void loadCartCacheIfNotExists(String userId){
        String cartKey = "cart:"+userId+":info";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartKey);
        int ttlInt = ttl.intValue();
        jedis.expire(cartKey, 10+ttlInt);
        Boolean exists = jedis.exists(cartKey);
        jedis.close();
        if (!exists){
            loadCartCache(userId);
        }
    }


    /**
     * 保存勾选状态 购物车商品的勾选
     * @param userId
     * @param skuId
     * @param isChecked
     */
    @Override
    public void checkCart(String userId, String skuId, String isChecked) {

        loadCartCacheIfNotExists(userId); // 检查一下缓存是否存在， 避免因缓存失效造成缓存和数据库不一致

        // ischeck数据 只保存在缓存中
        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);

        cartInfo.setIsChecked(isChecked);
        String cartInfoJsonNew = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey, skuId, cartInfoJsonNew);

        // 为了结账方便 把所有勾中的商品 单独存放在一个checked购物车中
        String cartCheckKey = "cart:" + userId + ":checked";
        if ("1".equals(isChecked)){ // 把勾中的商品加入到集合 取消勾中的商品就从集合中去掉
            jedis.hset(cartCheckKey, skuId, cartInfoJsonNew);
            jedis.expire(cartCheckKey, 60*30);
        } else {
            jedis.hdel(cartCheckKey, skuId);
        }

        loadCartCacheIfNotExists(userId);

        jedis.close();

    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        String cartCheckedKey = "cart:" + userId + ":checked";
        Jedis jedis = redisUtil.getJedis();

        List<String> checkedCartListJson = jedis.hvals(cartCheckedKey);
        List<CartInfo> cartInfoList = new ArrayList<>();
        for (String cartCheckedJson : checkedCartListJson) {

            CartInfo cartInfo = JSON.parseObject(cartCheckedJson, CartInfo.class);
            cartInfoList.add(cartInfo);

        }

        jedis.close();

        return cartInfoList;
    }

}
