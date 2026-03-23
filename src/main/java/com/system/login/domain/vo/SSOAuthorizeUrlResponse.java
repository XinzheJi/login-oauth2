package com.system.login.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * SSO授权URL响应
 */
@Data
@Builder
public class SSOAuthorizeUrlResponse {
    
    /**
     * OAuth2授权URL
     */
    private String authorizeUrl;
    
    /**
     * 状态参数
     */
    private String state;
    
    /**
     * 授权范围
     */
    private String scope;
}
