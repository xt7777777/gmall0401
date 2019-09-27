package com.atguigu.gmall0401.service;

import com.atguigu.gmall0401.bean.PaymentInfo;

/**
 * @author xtsky
 * @create 2019-09-24 20:13
 */
public interface PaymentInfoService {

    public void savePaymentInfo(PaymentInfo paymentInfo);

    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    public void updatePaymentInfoByOutTradeNo(String out_trade_no, PaymentInfo paymentInfoForUpdate);

    public void sendPaymentToOrder(String orderId, String result);

    public void sendDelayPaymentResult(String outTradeNo, Long delaySec, Integer checkCount);

}
