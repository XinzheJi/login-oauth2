package com.mock.a.controller;

import com.mock.a.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟OAuth2 Token获取接口
 */
@RestController
public class TokenController {

    private final TokenService tokenService;
    private final OAuth2AuthorizeController authorizeController;
    
    @Autowired
    public TokenController(TokenService tokenService, OAuth2AuthorizeController authorizeController) {
        this.tokenService = tokenService;
        this.authorizeController = authorizeController;
    }
    
    /**
     * OAuth2 token endpoint
     * 支持client_credentials、authorization_code和password模式
     */
    @PostMapping("/oauth/token")
    public ResponseEntity<?> getToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password) {
        
        if ("client_credentials".equals(grantType)) {
            // 客户端凭据模式
            Map<String, Object> tokenInfo = tokenService.generateToken(grantType, clientId, clientSecret);
            
            if (tokenInfo == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "invalid_client", "error_description", "客户端验证失败"));
            }
            
            return ResponseEntity.ok(tokenInfo);
            
        } else if ("authorization_code".equals(grantType)) {
            // 授权码模式
            if (code == null || redirectUri == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "invalid_request", "error_description", "缺少code或redirect_uri参数"));
            }
            
            // 验证客户端凭据
            if (!"b-system-client".equals(clientId) || !"b-system-secret".equals(clientSecret)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "invalid_client", "error_description", "客户端验证失败"));
            }
            
            // 验证授权码
            if (!authorizeController.validateAndConsumeAuthorizationCode(code, clientId, redirectUri)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "invalid_grant", "error_description", "无效的授权码"));
            }
            
            // 生成访问令牌
            Map<String, Object> tokenInfo = tokenService.generateToken("authorization_code", clientId, clientSecret);
            return ResponseEntity.ok(tokenInfo);
            
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "unsupported_grant_type", "error_description", "不支持的授权类型"));
        }
    }
    
    /**
     * OAuth2 用户信息端点
     */
    @GetMapping("/oauth/user")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authorization) {
        // 验证Bearer token
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_token", "error_description", "无效的访问令牌"));
        }
        
        String token = authorization.substring(7); // 移除"Bearer "前缀
        
        // 验证token
        if (!tokenService.validateToken(token)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_token", "error_description", "访问令牌已过期或无效"));
        }
        
        // 返回模拟用户信息（基于真实icomx_user表结构）
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", "12345");  // 对应数据库主键id
        userInfo.put("username", "sso_user"); // 对应account字段
        userInfo.put("name", "张三"); // 对应real_name字段  
        userInfo.put("email", "zhangsan@company.com");
        userInfo.put("phone", "13888888888"); 
        userInfo.put("email_verified", true);
        userInfo.put("picture", "https://avatar.example.com/sso_user.jpg"); // 对应avatar字段
        userInfo.put("tenant_id", "000000"); // 对应tenant_id字段
        userInfo.put("role_id", "1001,1002"); // 对应role_id字段（可能是逗号分隔）
        userInfo.put("dept_id", "2001"); // 对应dept_id字段
        userInfo.put("post_id", "3001"); // 对应post_id字段
        userInfo.put("status", 1); // 对应status字段，1表示启用
        userInfo.put("is_4A", "1"); // 对应is_4A字段，表示是4A用户
        userInfo.put("4A_id", "4A_USER_001"); // 对应_4A_id字段
        userInfo.put("org_id", "ORG_001"); // 对应org_id字段
        
        return ResponseEntity.ok(userInfo);
    }
} 