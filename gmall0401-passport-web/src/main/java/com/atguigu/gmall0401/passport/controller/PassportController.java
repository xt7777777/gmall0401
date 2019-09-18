package com.atguigu.gmall0401.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;
import com.atguigu.gmall0401.util.JwtUtil;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-18 21:12
 */
@Controller
public class PassportController {

    @Reference
    UserService userService;





    String jwtKey = "atguigu";


    @GetMapping("index")
    public String index(){
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        UserInfo user = userService.login(userInfo);
        if (user != null){
            // 制作token
            // 加密
            Map<String, Object> map = new HashMap<>();
            map.put("userId", user.getId());
            map.put("nickName", user.getNickName());

            // String remoteAddr = request.getRemoteAddr();//
            System.err.println(request.getRemoteAddr()); // 看下这个取出来的是什么 应该是nginx虚拟机ip地址

            String ipAddr = request.getHeader("X-forwarded-for");
            String token = JwtUtil.encode(jwtKey, map, ipAddr);
            return token;
        }

        return "fail";

    }


    @Test
    public void testJwt(){
        Map<String, Object> map = new HashMap<>();
        map.put("userId", "123");
        map.put("nickName", "zhang3");

        String token = JwtUtil.encode("Atguigu", map, "192.168.11.120");
        System.out.println(token);

        Map<String, Object> atguigu = JwtUtil.decode(token, "Atguigu", "192.168.11.122");
        System.err.println(atguigu);

    }


}
