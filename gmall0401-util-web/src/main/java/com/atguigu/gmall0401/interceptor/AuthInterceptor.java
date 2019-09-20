package com.atguigu.gmall0401.interceptor;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0401.config.LoginRequire;
import com.atguigu.gmall0401.constants.WebConst;
import com.atguigu.gmall0401.util.CookieUtil;
import com.atguigu.gmall0401.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xtsky
 * @create 2019-09-19 13:57
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = null ;
        // 检查token  token可能存在两个地方  1. url参数中 newToken  2. 从cookie中获得  token
        // 1 newToken的情况
        token = request.getParameter("newToken");
        if (token != null) {
            // 把token保存到cookie中
            CookieUtil.setCookie(request,response, "token", token, WebConst.cookieMaxAge, false);
        } else {
            // 从cookie中取值 token
            // token = "";  // from cookie
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        Map userMap = new HashMap();
        if (token != null) {
            // 如果token存在  从token中把用户信息取出来
            userMap = getUserMapFromToken(token);
            String nickName = (String) userMap.get("nickName");
            request.setAttribute("nickName", nickName);
        }

        // 判断该请求是否需要用户登录
        // 取到请求的方法上的注解 LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (loginRequire != null){
            // 需要认证
            if (token != null){
                // 要把token发给认证中心进行认证
                String currentIp = request.getHeader("X-forwarded-for");
                String result = HttpClientUtil.doGet(WebConst.VERIFY_URL + "?token=" + token + "&currentIP=" + currentIp);

                if ("success".equals(result)){
                    String userId = (String) userMap.get("userId");
                    request.setAttribute("userId", userId);
                    return true;
                } else if (!loginRequire.autoRedirect()){ // 认证失败 但是运行不跳转
                    return true;
                } else { // 认证失败 强行跳转
//                    String requestURL = request.getRequestURL().toString(); // 取得用户的当前登录请求
//                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
//                    response.sendRedirect(WebConst.LOGIN_URL + "?originUrl=" + encodeURL);
                    redirect(request,response);
                    return false;
                }
            } else {
                // 重定向 passport 让用户登录
//                String requestURL = request.getRequestURL().toString(); // 取得用户的当前登录请求
//                String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
//                response.sendRedirect(WebConst.LOGIN_URL + "?originUrl=" + encodeURL);
                if (!loginRequire.autoRedirect()){
                    return true;
                } else {
                    redirect(request, response);
                    return false;
                }
            }
        }

        return true;
    }


    private void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURL = request.getRequestURL().toString(); // 取得用户的当前登录请求
        String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
        response.sendRedirect(WebConst.LOGIN_URL + "?originUrl=" + encodeURL);
    }

    private Map getUserMapFromToken(String token){
        String userBase64 = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] userBates = base64UrlCodec.decode(userBase64);

        String userJson = new String(userBates);

        Map userMap = JSON.parseObject(userJson, Map.class);

        return userMap;

    }


}
