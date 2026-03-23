package com.system.login.controller;

import com.system.login.domain.entity.User;
import com.system.login.domain.vo.ApiResponse;
import com.system.login.domain.vo.LoginRequest;
import com.system.login.domain.vo.LoginResponse;
import com.system.login.domain.vo.UserInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import com.system.login.service.auth.UserService;
import com.system.login.service.auth.PermissionService;
import com.system.login.service.oauth2.OAuth2TokenService;
import com.system.login.domain.entity.Permission;
import com.system.login.domain.entity.Role;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final WebClient webClient;
    private final UserService userService;
    private final PermissionService permissionService;
    private final OAuth2TokenService oAuth2TokenService;
    
    @Autowired
    public AuthController(
            UserService userService,
            PermissionService permissionService,
            OAuth2TokenService oAuth2TokenService,
            WebClient.Builder webClientBuilder,
            @Value("${app.base-url:http://localhost:8081}") String baseUrl
    ) {
        this.userService = userService;
        this.permissionService = permissionService;
        this.oAuth2TokenService = oAuth2TokenService;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("收到本地登录请求: {}", loginRequest.getUsername());

        try {
            // 使用本地认证服务进行用户认证
            LoginResponse loginResponse = userService.login(loginRequest);
            
            return Mono.just(ResponseEntity.ok(ApiResponse.success(loginResponse)))
                    .doOnError(error -> log.error("本地登录失败: {}", error.getMessage()));
        } catch (Exception e) {
            log.error("本地登录失败", e);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("登录失败: " + e.getMessage())));
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/user/info")
    public ResponseEntity<UserInfoResponse> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("获取用户信息: {}", username);
        
        User user = userService.getByUsername(username);
        
        // 构建用户信息响应
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setId(user.getId());
        userInfoResponse.setUsername(user.getUsername());
        userInfoResponse.setRealName(user.getName()); // 使用name字段
        userInfoResponse.setEmail(""); // User实体暂无email字段
        userInfoResponse.setAvatar(""); // User实体暂无avatar字段
        userInfoResponse.setTenantId(user.getTenantId());
        userInfoResponse.setStatus(user.getStatus());
        userInfoResponse.setCreateTime(user.getCreateTime());
        userInfoResponse.setUpdateTime(user.getUpdateTime());
        userInfoResponse.setLoginMethod("local");
        
        // 获取用户权限
        try {
            List<Permission> permissions = permissionService.getPermissionsByUserId(user.getId());
            List<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(java.util.stream.Collectors.toList());
            userInfoResponse.setPermissions(permissionCodes);
            
            log.info("用户 {} 拥有权限: {}", username, permissionCodes);
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            userInfoResponse.setPermissions(new java.util.ArrayList<>());
        }
        

        userInfoResponse.setRoles(new java.util.ArrayList<>());
        
        return ResponseEntity.ok(userInfoResponse);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("用户退出登录: {}, 权限: {}", authentication.getName(), authentication.getAuthorities());
        } else {
            log.info("用户退出登录: 无认证信息");
        }
        
        // 清除OAuth2 Token缓存
        oAuth2TokenService.clearTokenCache();
        
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}