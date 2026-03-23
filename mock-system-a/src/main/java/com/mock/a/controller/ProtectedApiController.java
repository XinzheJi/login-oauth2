package com.mock.a.controller;

import com.mock.a.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟受保护的API接口
 */
@RestController
@RequestMapping("/api/protected")
public class ProtectedApiController {

    private final TokenService tokenService;
    
    @Autowired
    public ProtectedApiController(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    
    /**
     * 模拟受保护的数据接口
     */
    @GetMapping("/data")
    public ResponseEntity<?> getProtectedData(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 验证token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "unauthorized", "message", "缺少有效的授权令牌"));
        }
        
        String token = authHeader.substring(7); // 去掉"Bearer "前缀
        if (!tokenService.validateToken(token)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid_token", "message", "无效的令牌"));
        }
        
        // 返回模拟数据
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        
        Map<String, String> data = new HashMap<>();
        data.put("name", "Mocked Resource");
        data.put("value", "This is protected data");
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
} 