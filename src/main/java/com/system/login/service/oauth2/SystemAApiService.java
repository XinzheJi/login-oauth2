package com.system.login.service.oauth2;

import com.system.login.config.SystemAApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 系统A API服务，用于访问系统A的受保护接口
 */
@Slf4j
@Service
public class SystemAApiService {

    private final SystemAApiProperties apiProperties;
    private final OAuth2TokenService tokenService;
    private final RestTemplate restTemplate;
    
    @Autowired
    public SystemAApiService(SystemAApiProperties apiProperties, 
                            OAuth2TokenService tokenService) {
        this.apiProperties = apiProperties;
        this.tokenService = tokenService;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 获取受保护的数据
     */
    public ResponseEntity<Map> getProtectedData() {
        try {
            // 获取OAuth2访问令牌
            String accessToken = tokenService.getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("无法获取有效的访问令牌");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 构建请求头，携带访问令牌
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            
            // 发送请求
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = apiProperties.getFullProtectedDataUrl();
            
            log.info("正在请求系统A受保护接口: {}", url);
            return restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    Map.class);
            
        } catch (Exception e) {
            log.error("访问系统A API异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 