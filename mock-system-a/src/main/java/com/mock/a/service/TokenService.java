package com.mock.a.service;

import com.mock.a.config.MockOAuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {

    private final MockOAuthProperties properties;
    
    @Autowired
    public TokenService(MockOAuthProperties properties) {
        this.properties = properties;
    }
    
    /**
     * 验证客户端凭据并生成Token
     * 
     * @param grantType 授权类型
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @return Token信息Map，如果验证失败返回null
     */
    public Map<String, Object> generateToken(String grantType, String clientId, String clientSecret) {
        // 验证客户端凭据
        if (!properties.getClientId().equals(clientId) ||
                !properties.getClientSecret().equals(clientSecret)) {
            return null;
        }
        
        // 验证授权类型
        if (!"client_credentials".equals(grantType) && 
            !"password".equals(grantType) && 
            !"authorization_code".equals(grantType)) {
            return null;
        }
        
        // 生成Token响应
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("access_token", properties.getToken().getValue());
        tokenInfo.put("token_type", "bearer");
        tokenInfo.put("expires_in", properties.getToken().getExpiresIn());
        tokenInfo.put("scope", "read");
        
        return tokenInfo;
    }
    
    /**
     * 验证访问令牌是否有效
     * 
     * @param token 访问令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return properties.getToken().getValue().equals(token);
    }
} 