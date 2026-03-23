package com.system.login.controller;

import com.system.login.domain.entity.User;
import com.system.login.domain.vo.ApiResponse;
import com.system.login.domain.vo.LoginRequest;
import com.system.login.domain.vo.LoginResponse;
import com.system.login.domain.vo.UserInfoResponse;
import com.system.login.config.JwtConfig;
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
import com.system.login.service.security.JwtTokenService;
import com.system.login.domain.entity.Permission;
import com.system.login.domain.entity.Role;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import java.util.List;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;

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
    private final JwtTokenService jwtTokenService;
    private final JwtConfig jwtConfig;
    private final UserDetailsService userDetailsService;
    
    @Autowired
    public AuthController(
            UserService userService,
            PermissionService permissionService,
            OAuth2TokenService oAuth2TokenService,
            WebClient.Builder webClientBuilder,
            @Value("${app.base-url:http://localhost:8081}") String baseUrl,
            JwtTokenService jwtTokenService,
            JwtConfig jwtConfig,
            UserDetailsService userDetailsService
    ) {
        this.userService = userService;
        this.permissionService = permissionService;
        this.oAuth2TokenService = oAuth2TokenService;
        this.jwtTokenService = jwtTokenService;
        this.jwtConfig = jwtConfig;
        this.userDetailsService = userDetailsService;
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
        } catch (AuthenticationException e) {
            // 避免向前端暴露过多认证细节（防止用户枚举/内部信息泄露）
            log.warn("本地登录失败(认证不通过): {}", loginRequest.getUsername());
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户名或密码错误")));
        } catch (Exception e) {
            log.error("本地登录失败", e);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("登录失败")));
        }
    }
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/user/info")
    public ResponseEntity<UserInfoResponse> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || isAnonymous(authentication.getPrincipal())) {
            log.warn("获取用户信息失败：未找到有效的认证信息");
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = authentication.getName();
        log.info("获取用户信息: {}", username);
        
        User user = userService.getByUsername(username);
        if (user == null) {
            log.warn("获取用户信息失败：用户 {} 不存在或已被删除", username);
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
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

    private boolean isAnonymous(Object principal) {
        if (principal == null) {
            return true;
        }
        if (principal instanceof String str) {
            return "anonymousUser".equalsIgnoreCase(str);
        }
        return false;
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

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request) {
        String headerAuth = request.getHeader(jwtConfig.getTokenHeader());
        if (!StringUtils.hasText(headerAuth) || !headerAuth.startsWith(jwtConfig.getTokenPrefix() + " ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("未提供有效的令牌"));
        }

        String token = headerAuth.substring(jwtConfig.getTokenPrefix().length() + 1);
        try {
            if (jwtTokenService.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("令牌已过期"));
            }

            String username = jwtTokenService.getUsernameFromToken(token);
            User user = userService.getByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("用户不存在"));
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String businessTenantId = jwtTokenService.getTenantIdFromToken(token);

            String newToken = jwtTokenService.generateToken(user, userDetails, businessTenantId);
            long expiresIn = jwtConfig.getExpiration();
            long expiresAt = System.currentTimeMillis() + expiresIn * 1000;

            LoginResponse refreshResponse = LoginResponse.builder()
                    .token(newToken)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .userId(user.getId())
                    .tenantId(user.getTenantId())
                    .loginMethod("refresh")
                    .expiresIn(expiresIn)
                    .expiresAt(expiresAt)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(refreshResponse));
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("刷新令牌失败: " + e.getMessage()));
        }
    }
}
