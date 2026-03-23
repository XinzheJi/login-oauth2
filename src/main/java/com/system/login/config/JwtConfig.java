package com.system.login.config;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT密钥
     */
    private String secret;
    
    /**
     * JWT过期时间（秒）
     */
    private Long expiration;
    
    /**
     * Token请求头
     */
    private String tokenHeader;
    
    /**
     * Token前缀
     */
    private String tokenPrefix;
    
    /**
     * 生成JWT密钥
     */
    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
} 