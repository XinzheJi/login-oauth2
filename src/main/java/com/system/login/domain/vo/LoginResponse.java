package com.system.login.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 令牌类型
     */
    private String tokenType;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 登录方式 (local/sso)
     */
    private String loginMethod;

    /**
     * 令牌剩余有效期（秒）
     */
    private Long expiresIn;

    /**
     * 令牌过期时间戳（毫秒）
     */
    private Long expiresAt;
}
