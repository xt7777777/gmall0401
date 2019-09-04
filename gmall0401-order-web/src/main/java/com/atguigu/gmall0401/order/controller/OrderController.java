package com.atguigu.gmall0401.order.controller;

import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xtsky
 * @create 2019-09-04 21:14
 */
@RestController
public class OrderController {


    UserService userService;

    @GetMapping("trade")
    public UserInfo trade(String userId){

        UserInfo userInfo = userService.getUserInfo(userId);
        return userInfo;

    }

}
