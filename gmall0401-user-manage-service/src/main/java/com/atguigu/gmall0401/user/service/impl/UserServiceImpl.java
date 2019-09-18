package com.atguigu.gmall0401.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.bean.UserInfo;
import com.atguigu.gmall0401.service.UserService;
import com.atguigu.gmall0401.user.mapper.UserMapper;
import com.atguigu.gmall0401.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


/**
 * @author xtsky
 * @create 2019-09-04 19:52
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;



    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;




    @Override
    public List<UserInfo> getUserInfoListAll() {
        List<UserInfo> userInfos = userMapper.selectAll();
        return userInfos;
    }

    @Override
    public void addUser(UserInfo userInfo) {

        int i = userMapper.insertSelective(userInfo);
        System.out.println("insert Rows:" + i);

    }

    @Override
    public void updateUser(UserInfo userInfo) {

        userMapper.updateByPrimaryKeySelective(userInfo);

    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",name);
        userMapper.updateByExampleSelective(userInfo,example);
    }

    @Override
    public void delUser(String id) {

        userMapper.deleteByPrimaryKey(id);

    }

    @Override
    public UserInfo getUserInfo(String id) {
        UserInfo userInfo = userMapper.selectByPrimaryKey(id);
        return userInfo;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        // 1.比对数据库信息 用户名 密码
        String passwd = userInfo.getPasswd();
        String passwdMD5 = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(passwdMD5);

        UserInfo userInfo1 = userMapper.selectOne(userInfo);
        if (userInfo1 != null) {
            // 2.加载缓存
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix + userInfo1.getId() + userinfoKey_suffix;
            String userInfoJson = JSON.toJSONString(userInfo1);
            jedis.setex(userKey,userKey_timeOut,userInfoJson);
            jedis.close();
            return userInfo1;
        }
        return null;
    }
}
