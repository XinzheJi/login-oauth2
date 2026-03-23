package com.mock.a.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mock.oauth")
public class MockOAuthProperties {
    
    private String clientId;
    private String clientSecret;
    private TokenConfig token = new TokenConfig();
    
    @Data
    public static class TokenConfig {
        private String value;
        private int expiresIn;
    }
} 