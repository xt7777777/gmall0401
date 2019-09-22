package com.atguigu.gmall0401.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.CartInfo;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.service.CartService;
import com.atguigu.gmall0401.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

/**
 * @author xtsky
 * @create 2019-09-20 19:15
 */
@Controller
public class CartController {

    @Reference
    CartService cartService;




    @PostMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addCart(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num, HttpServletRequest request, HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");
        if (userId == null){
            // 如果用户未登录 检查用户是否有token 如果有token 作为id加购 如果没有 生成一个
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if (userId == null){
                userId = UUID.randomUUID().toString();
                CookieUtil.setCookie(request, response, "user_tmp_id", userId, 60*60*24*7, false);
            }
        }

        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
        request.setAttribute("cartInfo", cartInfo);
        request.setAttribute("num", num);

        return "success";

    }


    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request){

        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartList = null;
        List<CartInfo> cartTempList = null;
        String userTmpId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
        if (userId != null){
            if (userTmpId != null) {
                cartTempList = cartService.cartList(userTmpId);
                if (cartTempList != null && cartTempList.size() > 0) {
                    cartList = cartService.mergeCartList(userId, userTmpId);
                }
            }
            if (cartList == null || cartList.size() == 0){
                cartList = cartService.cartList(userId); // 如果不需要合并 去登陆后的购物车
            }
        } else {
            cartTempList = cartService.cartList(userTmpId);
            cartList = cartTempList;
        }

        request.setAttribute("cartList", cartList);

        return "cartList";

    }


    @PostMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(@RequestParam("isChecked") String isChecked, @RequestParam("skuId")String skuId, HttpServletRequest request){

        String userId = (String)request.getAttribute("userId");
        if (userId == null){
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
        }

        cartService.checkCart(userId, skuId, isChecked);


//        return "";

    }



}
