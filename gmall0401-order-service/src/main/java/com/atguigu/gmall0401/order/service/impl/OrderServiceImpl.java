package com.atguigu.gmall0401.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.OrderDetail;
import com.atguigu.gmall0401.bean.OrderInfo;
import com.atguigu.gmall0401.enums.ProcessStatus;
import com.atguigu.gmall0401.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0401.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0401.service.OrderService;
import com.atguigu.gmall0401.util.RedisUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    public String saveOrder(OrderInfo orderInfo) {

        orderInfoMapper.insertSelective(orderInfo);


        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);

        }

        return orderInfo.getId();

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

    @Override
    public OrderInfo getorderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(select);
        return orderInfo;
    }

    /**
     * 修改订单状态
     * @param orderId
     * @param processStatus
     */
    @Override
    public void updateStatus(String orderId, ProcessStatus processStatus, OrderInfo... orderInfos) {
        OrderInfo orderInfo = new OrderInfo();
        if (orderInfos != null && orderInfos.length > 0){
            orderInfo = orderInfos[0];
        }
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfo.setId(orderId);
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);


    }

    @Override
    public List<Integer> checkExpiredCoupon() {
        return Arrays.asList(1,2,3,4,5,6,7);
    }

    @Override
    @Async
    public void handleExpiredCoupon(Integer couponId) {

        try {
            System.out.println("购物券：" + couponId + "发送用户！");
            Thread.sleep(1000);

            System.out.println("购物券：" + couponId + "删除！");
            Thread.sleep(1000);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<Map> orderSplit(String orderId, String wareSkuMapJson) {

        // 先用orderid 查询出 原始订单
        OrderInfo orderInfoParent = getorderInfo(orderId);

        // wareSkuMap -> list 循环这个list
        List<Map> mapList = JSON.parseArray(wareSkuMapJson, Map.class);

        List<Map> wareParamMapList = new ArrayList<>();
        // 循环一次 ： 生成一个订单 订单生成2个部分 主订单orderinfo 订单明细
        for (Map wareSkuMap : mapList) {

            // 子订单 orderinfo 拷贝一份主订单信息 待修改金额 id parent order id
            OrderInfo orderInfoSub = new OrderInfo();
            try {
                BeanUtils.copyProperties(orderInfoSub, orderInfoParent);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            // 子订单 订单明细 orderDetail
            List<String> skuIds = (List<String>)wareSkuMap.get("skuIds"); // 传过来的拆单方案
            List<OrderDetail> orderDetailList = orderInfoParent.getOrderDetailList(); // 现有的父订单的所有订单明细
            ArrayList<OrderDetail> orderDetailSubList = new ArrayList<>(); // 我希望存放子订单的明细
            for (String skuId : skuIds) {

                for (OrderDetail orderDetail : orderDetailList) {

                    if (skuId.equals(orderDetail.getSkuId())){

                        OrderDetail orderDetailSub = new OrderDetail();
                        try {
                            BeanUtils.copyProperties(orderDetailSub, orderDetail);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }

                        orderDetailSub.setId(null);
                        orderDetailSub.setOrderId(null);
                        orderDetailSubList.add(orderDetailSub);

                    }

                }

            }


            // 组成完成一个子订单 修改金额 id清空 parent order id   保存子订单
            orderInfoSub.setOrderDetailList(orderDetailSubList);
            orderInfoSub.setId(null);
            orderInfoSub.sumTotalAmount();
            orderInfoSub.setParentOrderId(orderInfoParent.getId());

            saveOrder(orderInfoSub);

            // 把子订单包装成为库存模块 需要的结构 map
            Map wareParamMap = initWareParamJsonFormOrderInfo(orderInfoSub);
            Object wareId = wareSkuMap.get("wareId");
            wareParamMap.put("wareId", wareId);

            wareParamMapList.add(wareParamMap);

            // 原始订单的状态 改为已拆分
            updateStatus(orderId, ProcessStatus.SPLIT);


        }


        // 组合成为List<Map> 返回

        return wareParamMapList;
    }



    /**
     * 初始化发送到库存系统中的参数
     * @param orderId
     * @return
     */
    @Override
    public Map initWareParamJson(String orderId){

        OrderInfo orderInfo = getorderInfo(orderId);

        Map paramMap = initWareParamJsonFormOrderInfo(orderInfo);
//        String paramJson = JSON.toJSONString(paramMap);

        return paramMap;

    }


    /**
     * 封装数据
     * @param orderInfo
     * @return
     */
    private Map initWareParamJsonFormOrderInfo(OrderInfo orderInfo){

        Map<String, Object> paramMap = new HashMap();

        paramMap.put("orderId", orderInfo.getId());
        paramMap.put("consignee", orderInfo.getConsignee());
        paramMap.put("consigneeTel", orderInfo.getConsigneeTel());
        paramMap.put("orderComment", orderInfo.getOrderComment());
        paramMap.put("orderBody", orderInfo.genSubject());
        paramMap.put("deliveryAddress", orderInfo.getDeliveryAddress());
        paramMap.put("paymentWay", "2"); // 支付方式 1货到付款 2在线支付
        List<Map<String, String>> details = new ArrayList();
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            Map<String, String> orderDetailMap = new HashMap<>();
            orderDetailMap.put("skuId", orderDetail.getSkuId());
            orderDetailMap.put("skuNum", orderDetail.getSkuNum().toString());
            orderDetailMap.put("skuName", orderDetail.getSkuName());
            details.add(orderDetailMap);
        }
        paramMap.put("details", details);

        return paramMap;

    }


}
