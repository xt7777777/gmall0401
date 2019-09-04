package com.atguigu.gmall0401.service;


import com.atguigu.gmall0401.bean.UserInfo;

import java.util.List;

/**
 * @author xtsky
 * @create 2019-09-04 19:49
 */
public interface UserService {

    List<UserInfo> getUserInfoListAll();

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name,UserInfo userInfo);

    void delUser(String id);

    UserInfo getUserInfo(String id);
}
