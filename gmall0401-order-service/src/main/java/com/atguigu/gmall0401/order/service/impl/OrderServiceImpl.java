package com.atguigu.gmall0401.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0401.bean.OrderDetail;
import com.atguigu.gmall0401.bean.OrderInfo;
import com.atguigu.gmall0401.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0401.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0401.service.OrderService;
import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * @author xtsky
 * @create 2019-09-22 15:23
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    RedisUtil redisUtil;




    @Override
    @Transactional
    public void saveOrder(OrderInfo orderInfo) {

        orderInfoMapper.insertSelective(orderInfo);


        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);

        }

    }

    @Override
    public String genToken(String userId) {
        // token   type string  user    value token
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        String tokenKey = "user:" + userId + ":trade_code";
        Jedis jedis = redisUtil.getJedis();
        jedis.setex(tokenKey, 30*60, token);
        jedis.close();

        return token;
    }

    @Override
    public Boolean verifyToken(String userId, String token) {
        String tokenKey = "user:" + userId + ":trade_code";
        Jedis jedis = redisUtil.getJedis();
        String tokenExists = jedis.get(tokenKey);
        jedis.watch(tokenKey); // 监视tokenkey 开启事务
        Transaction transaction = jedis.multi();
        if (tokenExists != null && tokenExists.equals(token)){
            transaction.del(tokenKey);
        }
        List<Object> list = transaction.exec();
        if (list != null && list.size() > 0 && (Long)list.get(0) == 1L){
            return true;
        } else {
            return false;
        }
    }

    /**
     * 不用了 合并到上个方法了
     * @param userId
     */
    @Override
    public void delToken(String userId) {
        String tokenKey = "user:" + userId + ":trade_code";
        Jedis jedis = redisUtil.getJedis();
        jedis.del(tokenKey);

    }
}
