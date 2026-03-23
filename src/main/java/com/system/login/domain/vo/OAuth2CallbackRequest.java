package com.system.login.domain.vo;

import lombok.Data;

/**
 * OAuth2回调请求
 */
@Data
public class OAuth2CallbackRequest {
    
    /**
     * 授权码
     */
    private String code;
    
    /**
     * 状态参数（用于防CSRF攻击）
     */
    private String state;
    
    /**
     * 错误码（如果授权失败）
     */
    private String error;
    
    /**
     * 错误描述
     */
    private String errorDescription;
}
