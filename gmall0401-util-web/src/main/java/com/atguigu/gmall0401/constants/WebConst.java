package com.atguigu.gmall0401.constants;

/**
 * @author xtsky
 * @create 2019-09-19 14:18
 */
public interface WebConst {

    // 登录页面
    public static final String LOGIN_URL = "http://passport.gmall.com/index";

    // 认证接口
    public static final String VERIFY_URL = "http://passport.gmall.com/verify";

    //cookie的有效时间：默认给七天
    public static final int cookieMaxAge = 1*24*3600;

}
