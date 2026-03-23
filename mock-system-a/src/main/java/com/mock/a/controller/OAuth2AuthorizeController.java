package com.mock.a.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2授权端点控制器
 * 处理授权码模式的授权流程
 */
@Controller
public class OAuth2AuthorizeController {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public OAuth2AuthorizeController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * OAuth2授权端点
     * 显示授权同意页面
     */
    @GetMapping("/oauth/authorize")
    public String authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("scope") String scope,
            @RequestParam("state") String state,
            Model model) {

        // 验证参数
        if (!"code".equals(responseType)) {
            return "redirect:" + redirectUri + "?error=unsupported_response_type&state=" + state;
        }

        if (!"b-system-client".equals(clientId)) {
            return "redirect:" + redirectUri + "?error=invalid_client&state=" + state;
        }

        // 添加参数到模型中，用于页面显示和表单提交
        model.addAttribute("clientId", clientId);
        model.addAttribute("redirectUri", redirectUri);
        model.addAttribute("scope", scope);
        model.addAttribute("state", state);
        model.addAttribute("responseType", responseType);

        // 返回授权同意页面
        return "authorize";
    }

    /**
     * 处理用户授权同意
     */
    @PostMapping("/oauth/authorize")
    public String processAuthorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("scope") String scope,
            @RequestParam("state") String state,
            @RequestParam("response_type") String responseType,
            @RequestParam(value = "approve", required = false) String approve) {

        // 检查用户是否同意授权
        if (!"true".equals(approve)) {
            return "redirect:" + redirectUri + "?error=access_denied&state=" + state;
        }

        // 生成授权码
        String authorizationCode = "auth_code_" + UUID.randomUUID().toString().replace("-", "");
        
        // 将授权码存储到Redis，设置10分钟过期
        String codeKey = "oauth2:code:" + authorizationCode;
        String codeData = clientId + "###" + redirectUri + "###" + scope;
        redisTemplate.opsForValue().set(codeKey, codeData, 10, TimeUnit.MINUTES);

        // 重定向回系统B，携带授权码
        return "redirect:" + redirectUri + "?code=" + authorizationCode + "&state=" + state;
    }

    /**
     * 验证并消费授权码
     * 供TokenController调用
     */
    public boolean validateAndConsumeAuthorizationCode(String code, String clientId, String redirectUri) {
        String codeKey = "oauth2:code:" + code;
        String codeData = redisTemplate.opsForValue().get(codeKey);
        
        if (codeData == null) {
            return false; // 授权码不存在或已过期
        }
        
        // 验证授权码数据
        String[] parts = codeData.split("###");
        
        if (parts.length >= 2 && clientId.equals(parts[0]) && redirectUri.equals(parts[1])) {
            // 删除已使用的授权码（一次性使用）
            redisTemplate.delete(codeKey);
            return true;
        }
        
        return false;
    }
}
