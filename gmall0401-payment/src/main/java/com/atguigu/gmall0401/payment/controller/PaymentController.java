package com.atguigu.gmall0401.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall0401.bean.OrderInfo;
import com.atguigu.gmall0401.bean.PaymentInfo;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.enums.PaymentStatus;
import com.atguigu.gmall0401.payment.config.AlipayConfig;
import com.atguigu.gmall0401.service.OrderService;
import com.atguigu.gmall0401.service.PaymentInfoService;
import org.json.JSONException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-23 21:10
 */
@Controller
public class PaymentController {


    @Reference
    OrderService orderService;

    @Autowired
    AlipayClient alipayClient;

    @Reference
    PaymentInfoService paymentInfoService;




    @GetMapping("index")
    @LoginRequire
    public String index(String orderId, HttpServletRequest request) {
        OrderInfo orderInfo = orderService.getorderInfo(orderId);

        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());

        return "index";

    }


    @PostMapping("/alipay/submit")
    @ResponseBody
    public String alipaySubmit(String orderId, HttpServletResponse response){

        // 1. 准备参数 给支付宝提交
        OrderInfo orderInfo = orderService.getorderInfo(orderId);

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        long currentTimeMillis = System.currentTimeMillis();
        String outTradeNo = "XTSKY" + orderId + "-" + currentTimeMillis;
        String productNo = "FAST_INSTANT_TRADE_PAY";
        BigDecimal totalAmount = orderInfo.getTotalAmount();
        String subject = orderInfo.genSubject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("product_code", productNo);
        jsonObject.put("total_amount", totalAmount);
        jsonObject.put("subject", subject);
        alipayRequest.setBizContent(jsonObject.toJSONString());

        // 组织参数
        String submitHtml = "";
        try {
            submitHtml = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");

        // 2. 把提交操作 保存起来~

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(totalAmount);

        paymentInfoService.savePaymentInfo(paymentInfo);

        paymentInfoService.sendDelayPaymentResult(outTradeNo, 30L, 3);

        return submitHtml;

    }


    @PostMapping("/alipay/callback/notify")
//    @ResponseBody
    public String notify(@RequestParam Map<String, String> paramMap, HttpServletRequest request) throws AlipayApiException {

//        if (1 == 1){
//            return "";
//        }


        // 验签  支付宝公钥 数据

        // 判断成功失败标志

        // 判断一下 当前支付状态的状态  未支付 更改支付状态

        // 用户订单状态  仓储发货  异步方式处理

        // 返回 success 标志

        String sign = paramMap.get("sign");
        boolean isPass = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8", AlipayConfig.sign_type);
        if (isPass){

            String tradeStatus = paramMap.get("trade_status");
            String total_amount = paramMap.get("total_amount");
            String out_trade_no = paramMap.get("out_trade_no");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {

                PaymentInfo paymentInfoQuery = new PaymentInfo();
                paymentInfoQuery.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(paymentInfoQuery);
                if (paymentInfo.getTotalAmount().compareTo(new BigDecimal(total_amount)) == 0 ){

                    // 判断一下 当前支付状态的状态
                    if (paymentInfo.getPaymentStatus().equals(PaymentStatus.UNPAID)){
                        // 更新 状态
                        PaymentInfo paymentInfoForUpdate = new PaymentInfo();
                        paymentInfoForUpdate.setPaymentStatus(PaymentStatus.PAID);
                        paymentInfoForUpdate.setCallbackTime(new Date());
                        paymentInfoForUpdate.setCallbackContent(JSON.toJSONString(paramMap));
                        paymentInfoForUpdate.setAlipayTradeNo(paramMap.get("trade_no"));

                        paymentInfoService.updatePaymentInfoByOutTradeNo(out_trade_no, paymentInfoForUpdate);


                        // 发送异步消息 给订单


                        return "success";
                    } else if (paymentInfo.getPaymentStatus().equals(PaymentStatus.ClOSED)){
                        // 手工发送关单操作
                        return "fail";
                    } else if (paymentInfo.getPaymentStatus().equals(PaymentStatus.PAID)){
                        return "success";
                    }

                }

            }

        }
        return "fail";

    }


    /**
     * 放消息队列里   直接发送 支付成功了~~
     * @param orderId
     * @return
     */
    @GetMapping("sendPayment")
    @ResponseBody
    public String sendPayment(String orderId){

        paymentInfoService.sendPaymentToOrder(orderId, "success");

        return "success";

    }




    @GetMapping("/alipay/callback/return")
    @ResponseBody
    public String alipayReturn(){
        return "交易成功";
    }


    @GetMapping("refund")
    @ResponseBody
    public String refund(String orderId) throws AlipayApiException {

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderId);
        PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(paymentInfoQuery);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", paymentInfo.getOutTradeNo());
        jsonObject.put("refund_amount", paymentInfo.getTotalAmount());
        request.setBizContent(jsonObject.toJSONString());
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        if (response.isSuccess()){
            System.out.println("调用成功");
            // 不用判断 返回空说明成功 返回东西说明失败 if (response.getSubCode().equals("ACQ.TRADE_HAS_SUCCESS")){
            System.out.println("业务退款成功");
            PaymentInfo paymentInfoUpdate = new PaymentInfo();
            paymentInfoUpdate.setPaymentStatus(PaymentStatus.PAY_REFUND);
            paymentInfoService.updatePaymentInfoByOutTradeNo(paymentInfo.getOutTradeNo(), paymentInfoUpdate);
            // 处理订单状态
            // 异步处理
            return "success";

        } else {
            System.out.println("调用失败");
            return response.getSubCode() + ":" + response.getSubMsg();
            //  return "fail";

        }

    }


}
