package com.system.login.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2客户端配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oauth2.client")
public class OAuth2ClientProperties {
    
    /**
     * Token获取地址
     */
    private String tokenUrl;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 客户端密钥
     */
    private String clientSecret;
    
    /**
     * 权限范围
     */
    private String scope;
} 