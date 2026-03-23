package com.system.login.service.oauth2;

import com.system.login.config.OAuth2ClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2 Token服务，负责获取和管理系统A的访问令牌
 */
@Slf4j
@Service
public class OAuth2TokenService {

    private final OAuth2ClientProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    
    /**
     * Redis中缓存Token的键前缀
     */
    private static final String TOKEN_CACHE_KEY = "oauth2:token:";
    
    @Autowired
    public OAuth2TokenService(OAuth2ClientProperties properties, 
                             StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 获取访问令牌，优先从缓存获取，如果缓存不存在则重新请求
     */
    public String getAccessToken() {
        // 尝试从Redis缓存获取
        String cacheKey = TOKEN_CACHE_KEY + properties.getClientId();
        String token = null;
        
        try {
            token = redisTemplate.opsForValue().get(cacheKey);
            log.debug("从Redis缓存获取token: {}", (token != null ? "成功" : "失败"));
        } catch (Exception e) {
            log.warn("从Redis获取Token时发生异常，将直接请求新Token: {}", e.getMessage());
            // 出现异常时不抛出，继续尝试请求新Token
        }
        
        // 如果缓存中没有，则请求新Token
        if (token == null || token.isEmpty()) {
            log.info("OAuth2 token不存在或已过期，重新获取");
            return requestNewToken();
        }
        
        return token;
    }
    
    /**
     * 请求新的访问令牌
     */
    @SuppressWarnings("unchecked")
    private String requestNewToken() {
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "client_credentials");
            params.add("client_id", properties.getClientId());
            params.add("client_secret", properties.getClientSecret());
            if (properties.getScope() != null && !properties.getScope().isEmpty()) {
                params.add("scope", properties.getScope());
            }
            
            // 发送请求
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    properties.getTokenUrl(), 
                    request, 
                    Map.class);
            
            // 解析响应
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String accessToken = (String) body.get("access_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                
                // 缓存Token，设置过期时间比实际稍短，避免边界问题
                if (accessToken != null && expiresIn != null) {
                    String cacheKey = TOKEN_CACHE_KEY + properties.getClientId();
                    // 提前5分钟过期，避免边界情况
                    int cacheExpiry = expiresIn - 300;
                    if (cacheExpiry <= 0) {
                        cacheExpiry = expiresIn;
                    }
                    redisTemplate.opsForValue().set(cacheKey, accessToken, cacheExpiry, TimeUnit.SECONDS);
                    log.info("获取OAuth2 token成功，有效期{}秒", expiresIn);
                    return accessToken;
                }
            }
            
            log.error("获取OAuth2 token失败，响应状态码: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("请求OAuth2 token异常", e);
        }
        
        return null;
    }
    
    /**
     * 清除令牌缓存
     */
    public void clearTokenCache() {
        String cacheKey = TOKEN_CACHE_KEY + properties.getClientId();
        redisTemplate.delete(cacheKey);
        log.info("已清除OAuth2 token缓存");
    }
} 