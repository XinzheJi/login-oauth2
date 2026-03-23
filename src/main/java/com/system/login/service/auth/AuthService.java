package com.system.login.service.auth;

import com.system.login.domain.vo.LoginRequest;
import com.system.login.domain.vo.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应结果
     */
    LoginResponse login(LoginRequest loginRequest);
} 