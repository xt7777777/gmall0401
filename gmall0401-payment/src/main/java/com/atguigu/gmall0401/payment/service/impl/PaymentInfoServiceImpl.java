package com.atguigu.gmall0401.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0401.bean.PaymentInfo;
import com.atguigu.gmall0401.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall0401.service.PaymentInfoService;
import com.atguigu.gmall0401.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

/**
 * @author xtsky
 * @create 2019-09-24 20:14
 */
@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;





    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {

        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQuery);

        return paymentInfo;
    }

    @Override
    public void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfoForUpdate) {

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoForUpdate, example);

    }

    @Override
    public void sendPaymentToOrder(String orderId, String result) {

        Connection connection = activeMQUtil.getConnection();

        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            MessageProducer producer = session.createProducer(session.createQueue("PAYMENT_TO_ORDER"));
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId", orderId);
            mapMessage.setString("result", result);

            producer.send(mapMessage);

            session.commit(); // 事务是true 需要commit
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
