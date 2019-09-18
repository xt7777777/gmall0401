package com.atguigu.gmall0401.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xtsky
 * @create 2019-09-18 21:12
 */
@Controller
public class PassportController {

    @Reference
    UserService userService;




    @GetMapping("index")
    public String index(){
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo){

        Boolean isLogin = userService.login(userInfo);
        return isLogin.toString();

    }


}
