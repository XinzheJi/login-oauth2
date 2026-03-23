package com.system.login.service.auth;

import com.system.login.domain.vo.LoginResponse;
import com.system.login.domain.vo.OAuth2CallbackRequest;
import com.system.login.domain.vo.SSOAuthorizeUrlResponse;

/**
 * 混合认证服务接口
 * 支持本地登录和SSO登录
 */
public interface HybridAuthService {
    
    /**
     * 生成SSO授权URL
     * 
     * @return SSO授权URL响应
     */
    SSOAuthorizeUrlResponse generateSSOAuthorizeUrl();
    
    /**
     * 处理OAuth2回调
     * 
     * @param request OAuth2回调请求
     * @return 登录响应
     */
    LoginResponse processOAuth2Callback(OAuth2CallbackRequest request);
    
    /**
     * 验证状态参数
     * 
     * @param state 状态参数
     * @return 是否有效
     */
    boolean validateState(String state);
}
