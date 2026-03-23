package com.system.login.controller;

import com.system.login.domain.vo.LoginResponse;
import com.system.login.domain.vo.OAuth2CallbackRequest;
import com.system.login.domain.vo.SSOAuthorizeUrlResponse;
import com.system.login.service.auth.HybridAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * 混合认证控制器
 * 处理本地登录和SSO登录
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class HybridAuthController {

    private final HybridAuthService hybridAuthService;

    @Value("${hybrid-auth.frontend.callback-url:http://localhost:8080/#/login}")
    private String frontendCallbackUrl;

    @Autowired
    public HybridAuthController(HybridAuthService hybridAuthService) {
        this.hybridAuthService = hybridAuthService;
    }

    /**
     * 检查SSO是否启用
     */
    @GetMapping("/sso/enabled")
    public ResponseEntity<Map<String, Object>> isSSOEnabled() {
        return ResponseEntity.ok(Map.of(
                "enabled", true,
                "provider", "System A"
        ));
    }

    /**
     * 获取SSO授权URL
     * 前端调用此接口获取OAuth2授权URL，然后重定向用户到授权服务器
     */
    @GetMapping("/sso/authorize-url")
    public ResponseEntity<SSOAuthorizeUrlResponse> getSSOAuthorizeUrl() {
        log.info("生成SSO授权URL请求");
        
        try {
            SSOAuthorizeUrlResponse response = hybridAuthService.generateSSOAuthorizeUrl();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("生成SSO授权URL失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 处理OAuth2授权回调
     * 这是OAuth2授权服务器回调的端点
     */
    @GetMapping("/oauth2/callback")
    public RedirectView handleOAuth2CallbackGet(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        
        // code/state 属于敏感信息，避免落日志
        log.info("接收到OAuth2回调: code存在={}, state存在={}, error={}",
                StringUtils.hasText(code), StringUtils.hasText(state), error);

        try {
            OAuth2CallbackRequest request = new OAuth2CallbackRequest();
            request.setCode(code);
            request.setState(state);
            request.setError(error);
            request.setErrorDescription(errorDescription);

            if (error != null) {
                // 授权失败，重定向到前端登录页面并显示错误
                String redirectUrl = buildCallbackUrl(Map.of(
                        "error", error,
                        "error_description", errorDescription != null ? errorDescription : "授权失败"
                ));
                return new RedirectView(redirectUrl);
            }

            // 处理OAuth2回调
            LoginResponse loginResponse = hybridAuthService.processOAuth2Callback(request);
            
            // 成功登录，重定向到前端并携带JWT令牌
            Map<String, String> params = new LinkedHashMap<>();
            params.put("token", loginResponse.getToken());
            params.put("method", "sso");
            params.put("username", loginResponse.getUsername());
            String redirectUrl = buildCallbackUrl(params);
            
            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            log.error("处理OAuth2回调失败", e);
            String redirectUrl = buildCallbackUrl(Map.of(
                    "error", "callback_failed",
                    "error_description", e.getMessage() != null ? e.getMessage() : "SSO回调失败"
            ));
            return new RedirectView(redirectUrl);
        }
    }

    /**
     * 处理OAuth2授权回调 (POST方式)
     * 提供POST接口支持，用于前端AJAX调用
     */
    @PostMapping("/oauth2/callback")
    public ResponseEntity<LoginResponse> handleOAuth2CallbackPost(@RequestBody OAuth2CallbackRequest request) {
        // code/state 属于敏感信息，避免落日志
        log.info("接收到OAuth2回调POST请求: code存在={}, state存在={}",
                StringUtils.hasText(request.getCode()), StringUtils.hasText(request.getState()));

        try {
            if (request.getError() != null) {
                log.error("OAuth2授权失败: {} - {}", request.getError(), request.getErrorDescription());
                return ResponseEntity.badRequest().build();
            }

            LoginResponse response = hybridAuthService.processOAuth2Callback(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("处理OAuth2回调失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 启动SSO登录流程
     * 直接重定向到OAuth2授权服务器
     */
    @GetMapping("/sso/login")
    public RedirectView initiateSSO() {
        log.info("启动SSO登录流程");
        
        try {
            SSOAuthorizeUrlResponse response = hybridAuthService.generateSSOAuthorizeUrl();
            return new RedirectView(response.getAuthorizeUrl());
        } catch (Exception e) {
            log.error("启动SSO登录失败", e);
            String redirectUrl = buildCallbackUrl(Map.of(
                    "error", "sso_init_failed",
                    "error_description", e.getMessage() != null ? e.getMessage() : "SSO初始化失败"
            ));
            return new RedirectView(redirectUrl);
        }
    }

    private String buildCallbackUrl(Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return frontendCallbackUrl;
        }
        StringBuilder builder = new StringBuilder(frontendCallbackUrl);
        char separator = frontendCallbackUrl.contains("?") ? '&' : '?';
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null) {
                continue;
            }
            builder.append(separator)
                    .append(urlEncode(key))
                    .append("=")
                    .append(urlEncode(value));
            separator = '&';
        }
        return builder.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
