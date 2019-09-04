package com.atguigu.gmall0401.user.controller;


import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-04 20:02
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("addUser")
    public  String  addUser(  UserInfo userInfo){
        userService.addUser(userInfo);
        return "success";
    }

    @GetMapping("allUser")
    public List<UserInfo> getAllUser(){
        return  userService.getUserInfoListAll();
    }

    @GetMapping("userInfo")
    public UserInfo getUser(@RequestParam("id") String id){
        return  userService.getUserInfo(id);
    }

    @PostMapping("delUser")
    public String deleteUser(String id){
        userService.delUser(id);
        return  "success";
    }

    @PostMapping("updateUser")
    public String updateUser(UserInfo userInfo){
        //  userService.updateUser(userInfo);

        userService.updateUserByName(userInfo.getName(),userInfo);
        return  "success";
    }

}