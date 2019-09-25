package com.atguigu.gmall0401.order.consumer;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author xtsky
 * @create 2019-09-25 23:28
 */
@Component
public class OrderConsumer {



    @JmsListener(destination = "PAYMENT_TO_ORDER", containerFactory = "jmsQueueListener")
    public void consumePayment(MapMessage mapMessage) throws JMSException {

        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        if("success".equals(result)){
            System.out.println("订单：" + orderId + "支付完成");
            System.out.println("result : " + result);
        }

    }


}
