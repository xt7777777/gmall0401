package com.atguigu.gmall0401.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xtsky
 * @create 2019-09-19 14:33
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // source : 编译class就没了   class：编译class还在 java运行不在了   runtime
public @interface LoginRequire {

    boolean autoRedirect() default true; // 自动重定向

}
