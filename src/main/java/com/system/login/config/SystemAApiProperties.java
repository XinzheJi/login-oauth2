package com.system.login.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统A API配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "system-a.api")
public class SystemAApiProperties {
    
    /**
     * 系统A基础URL
     */
    private String baseUrl;
    
    /**
     * 受保护数据接口URL
     */
    private String protectedDataUrl;
    
    /**
     * 获取完整的受保护数据接口URL
     */
    public String getFullProtectedDataUrl() {
        return baseUrl + protectedDataUrl;
    }
} 