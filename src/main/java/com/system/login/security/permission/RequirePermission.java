package com.system.login.security.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限控制注解
 * 用于标记需要特定权限才能访问的方法或类
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 所需权限编码
     */
    String value();
    
    /**
     * 是否检查租户
     * 默认为true，表示在多租户环境下进行权限检查
     */
    boolean checkTenant() default true;
}