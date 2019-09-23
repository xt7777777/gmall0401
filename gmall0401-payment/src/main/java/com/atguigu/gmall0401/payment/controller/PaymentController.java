package com.atguigu.gmall0401.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.OrderInfo;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author xtsky
 * @create 2019-09-23 21:10
 */
@Controller
public class PaymentController {


    @Reference
    OrderService orderService;



    @GetMapping("index")
    @LoginRequire
    public String index(String orderId, HttpServletRequest request) {
        OrderInfo orderInfo = orderService.getorderInfo(orderId);

        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());

        return "index";

    }

}
