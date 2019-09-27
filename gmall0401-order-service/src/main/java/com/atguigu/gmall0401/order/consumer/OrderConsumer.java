package com.atguigu.gmall0401.order.consumer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.OrderDetail;
import com.atguigu.gmall0401.bean.OrderInfo;
import com.atguigu.gmall0401.enums.ProcessStatus;
import com.atguigu.gmall0401.service.OrderService;
import com.atguigu.gmall0401.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-25 23:28
 */
@Component
public class OrderConsumer {

    @Reference
    OrderService orderService;

    @Autowired
    ActiveMQUtil activeMQUtil;




    @JmsListener(destination = "PAYMENT_TO_ORDER", containerFactory = "jmsQueueListener")
    public void consumePayment(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        if("success".equals(result)){
            System.out.println("订单：" + orderId + "支付完成");
            System.out.println("result : " + result);

            // 订单修改状态
            orderService.updateStatus(orderId, ProcessStatus.PAID);

            // 发送消息给库存系统
            sendOrderToWare(orderId);

        }

    }


    public void sendOrderToWare(String orderId){

        Map paramMap = orderService.initWareParamJson(orderId);
        String wareParamJson = JSON.toJSONString(paramMap);

        Connection connection = activeMQUtil.getConnection();
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            MessageProducer producer = session.createProducer(session.createQueue("ORDER_RESULT_QUEUE"));

            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(wareParamJson);
            producer.send(textMessage);
            orderService.updateStatus(orderId, ProcessStatus.NOTIFIED_WARE); // 修改订单状态 后台

            session.commit();

            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }



    /**
     * 消费 减库存
     * @param mapMessage
     * @throws JMSException
     */
    @JmsListener(destination = "SKU_DEDUCT_QUEUE", containerFactory = "jmsQueueListener") // , concurrency = "3" 这个并发 不知道行不行
    public void consumeWareDeduct(MapMessage mapMessage) throws JMSException {

        // 更新订单状态
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        if ("DEDUCTED".equals(status)){
            orderService.updateStatus(orderId, ProcessStatus.WAITING_DELEVER); // 待发货
        } else {
            orderService.updateStatus(orderId, ProcessStatus.STOCK_EXCEPTION); // 库存异常
        }

    }


    /**
     * 消费 发货状态
     */
    @JmsListener(destination = "SKU_DELIVER_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeDeliver(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");
        String trackingNo = mapMessage.getString("trackingNo");

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTrackingNo(trackingNo);
        if (status.equals("DELEVERED")){
            orderService.updateStatus(orderId, ProcessStatus.DELEVERED, orderInfo);
        }

    }




}
