package com.atguigu.gmall0401.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.*;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.enums.OrderStatus;
import com.atguigu.gmall0401.enums.ProcessStatus;
import com.atguigu.gmall0401.service.CartService;
import com.atguigu.gmall0401.service.ManageService;
import com.atguigu.gmall0401.service.OrderService;
import com.atguigu.gmall0401.service.UserService;
import com.atguigu.gmall0401.util.HttpClientUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.management.relation.Relation;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author xtsky
 * @create 2019-09-04 21:14
 */
@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;


    /**
     * 结算页面 要进
     * @param request
     * @return
     */
    @GetMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");

        // 用户地址 列表
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);


        // 用户需要结账的商品清单
        List<CartInfo> checkedCartList = cartService.getCheckedCartList(userId);
        BigDecimal totalAmount = new BigDecimal("0");
        for (CartInfo cartInfo : checkedCartList) {
            BigDecimal multiply = cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            totalAmount = totalAmount.add(multiply);
        }

        String token = orderService.genToken(userId);
        request.setAttribute("tradeNo", token);

        request.setAttribute("checkedCartList",checkedCartList);
        request.setAttribute("totalAmount", totalAmount);

        return "trade";

    }


    /**
     * 结算去 提交订单付款
     * @param orderInfo
     * @param request
     * @return
     */
    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request){
        String tradeNo = request.getParameter("tradeNo");
        String userId = (String)request.getAttribute("userId");
        Boolean isEnableToken = orderService.verifyToken(userId, tradeNo);
        if (!isEnableToken){
            request.setAttribute("errMsg", "页面重复提交！");
            return "tradeFail";
        }

        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addMinutes(new Date(), 15));
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {

            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName());

            if (!orderDetail.getOrderPrice().equals(skuInfo.getPrice())){

                request.setAttribute("errMsg", "商品价格已发生变动，请重新下单！");
                return "tradeFail";

            }

        }

        List<OrderDetail> errList = Collections.synchronizedList(new ArrayList<>());
        Stream<CompletableFuture<String>> completableFutureStream = orderDetailList.stream().map(orderDetail ->
                CompletableFuture.supplyAsync(() -> checkSkuNum(orderDetail)).whenComplete((hasStock, ex) -> {
                    if (hasStock.equals("0")) {
                        errList.add(orderDetail);
                    }
                })
        );
        CompletableFuture[] completableFutures = completableFutureStream.toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

        if (errList.size() > 0){
            StringBuffer errStringBuffer = new StringBuffer();
            for (OrderDetail orderDetail : errList) {
                errStringBuffer.append("商品：" + orderDetail.getSkuName() + "库存不足!");
            }
            request.setAttribute("errMsg", errStringBuffer.toString());
            return "tradeFail";
        }

        /*for (OrderDetail orderDetail : orderDetailList) {

            String hasStock = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());

            if ("1".equals(hasStock)){


            } else {


            }

        }*/

        String orderId = orderService.saveOrder(orderInfo);

        // 删除购物车信息
        // ... 购物车缓存删掉 未加~  ddd

        return "redirect://payment.gmall.com/index?orderId=" + orderId;

    }

    public String checkSkuNum(OrderDetail orderDetail){
        String hasStock = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());
        return hasStock;
    }




    // 1,2,3,4,5,6,7,8,9,  list       找出所有能被3整除的数 放到一个清单里
    @Test
    public void test1(){
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9);
//        List rsList = new ArrayList();
        List rsList = Collections.synchronizedList(new ArrayList<>());
        Stream<CompletableFuture<Boolean>> completableFutureStream = list.stream().map(num ->
                CompletableFuture.supplyAsync(() -> checkNum(num)).whenComplete((ifPass, ex) -> {
                    if (ifPass) {
                        rsList.add(num);
                    }
                }));
        CompletableFuture[] completableFutures = completableFutureStream.toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures).join();

//        list.stream().map(num -> {
//            if (checkNum(num)) {
//                rsList.add(num);
//            }
//            return num;
//        }).toArray(Integer[]::new);

        System.out.println(rsList);

    }

    private Boolean checkNum(Integer num){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (num % 3 == 0){
            return true;
        } else {
            return false;
        }
    }



}
