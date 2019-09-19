package com.atguigu.gmall0401.config;

import com.atguigu.gmall0401.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author xtsky
 * @create 2019-09-19 13:54
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    // 注册一个拦截器
    @Autowired
    AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(authInterceptor).addPathPatterns("/**");

    }


}
