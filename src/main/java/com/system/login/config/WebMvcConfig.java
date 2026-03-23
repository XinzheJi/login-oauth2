package com.system.login.config;

import com.system.login.security.permission.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 用于注册拦截器等Web相关配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;
    
    @Autowired
    public WebMvcConfig(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册权限拦截器，拦截所有API请求
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                // 排除不需要权限控制的路径
                .excludePathPatterns("/api/auth/login", "/api/health");
    }
}