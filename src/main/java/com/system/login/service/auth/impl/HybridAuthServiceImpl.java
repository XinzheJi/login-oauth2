package com.system.login.service.auth.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.login.domain.entity.User;
import com.system.login.domain.vo.LoginResponse;
import com.system.login.domain.vo.OAuth2CallbackRequest;
import com.system.login.domain.vo.OAuth2UserInfo;
import com.system.login.domain.vo.SSOAuthorizeUrlResponse;
import com.system.login.mapper.UserMapper;
import com.system.login.service.auth.HybridAuthService;
import com.system.login.service.security.JwtTokenService;
import com.system.login.service.tenant.TenantService;
import com.system.login.mapper.UserRoleMapper;
import com.system.login.domain.entity.UserRole;
import com.system.login.config.JwtConfig;
import com.system.login.security.cache.UserDetailsCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 混合认证服务实现
 */
@Slf4j
@Service
public class HybridAuthServiceImpl implements HybridAuthService {

    private final UserMapper userMapper;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;
    private final TenantService tenantService;
    private final UserRoleMapper userRoleMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsCacheHelper userDetailsCacheHelper;

    @Value("${hybrid-auth.sso.client-id:b-system-client}")
    private String clientId;

    @Value("${hybrid-auth.sso.client-secret:b-system-secret}")
    private String clientSecret;

    @Value("${hybrid-auth.sso.authorization-uri:http://localhost:8080/oauth/authorize}")
    private String authorizationUri;

    @Value("${hybrid-auth.sso.token-uri:http://localhost:8080/oauth/token}")
    private String tokenUri;

    @Value("${hybrid-auth.sso.user-info-uri:http://localhost:8080/oauth/user}")
    private String userInfoUri;

    @Value("${hybrid-auth.sso.redirect-uri:http://localhost:8081/api/auth/oauth2/callback}")
    private String redirectUri;

    @Value("${hybrid-auth.sso.default-tenant-id:1}")
    private Long defaultTenantId;

    @Value("${hybrid-auth.sso.auto-create-user:true}")
    private boolean autoCreateUser;
    
    @Value("${hybrid-auth.sso.default-role-ids:2}")
    private String defaultRoleIds;

    @Autowired
    public HybridAuthServiceImpl(UserMapper userMapper,
                                JwtTokenService jwtTokenService,
                                UserDetailsService userDetailsService,
                                StringRedisTemplate redisTemplate,
                                TenantService tenantService,
                                UserRoleMapper userRoleMapper,
                                JwtConfig jwtConfig,
                                PasswordEncoder passwordEncoder,
                                UserDetailsCacheHelper userDetailsCacheHelper) {
        this.userMapper = userMapper;
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
        this.tenantService = tenantService;
        this.userRoleMapper = userRoleMapper;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.jwtConfig = jwtConfig;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsCacheHelper = userDetailsCacheHelper;
    }

    @Override
    public SSOAuthorizeUrlResponse generateSSOAuthorizeUrl() {
        // 生成随机状态参数防止CSRF攻击
        String state = UUID.randomUUID().toString();
        
        // 将状态参数存储到Redis，设置10分钟过期
        String stateKey = "oauth2:state:" + state;
        redisTemplate.opsForValue().set(stateKey, "valid", 10, TimeUnit.MINUTES);

        // 构建授权URL
        String scope = "openid profile email";
        String responseType = "code";
        
        String authorizeUrl = String.format(
                "%s?response_type=%s&client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                authorizationUri, responseType, clientId, redirectUri, scope, state
        );

        // 避免在日志中输出包含 state 的完整URL（可能被用于绕过CSRF校验）
        log.debug("生成SSO授权URL，authorizationUri={}, clientId={}, redirectUri={}, scope={}",
                authorizationUri, clientId, redirectUri, scope);

        return SSOAuthorizeUrlResponse.builder()
                .authorizeUrl(authorizeUrl)
                .state(state)
                .scope(scope)
                .build();
    }

    @Override
    public LoginResponse processOAuth2Callback(OAuth2CallbackRequest request) {
        // code/state 属于敏感信息，避免落日志
        log.info("处理OAuth2回调，code存在={}, state存在={}",
                StringUtils.hasText(request.getCode()), StringUtils.hasText(request.getState()));

        // 检查是否有错误
        if (request.getError() != null) {
            log.error("OAuth2授权失败: {} - {}", request.getError(), request.getErrorDescription());
            throw new RuntimeException("OAuth2授权失败: " + request.getErrorDescription());
        }

        // 验证状态参数
        if (!validateState(request.getState())) {
            log.error("无效的状态参数: {}", request.getState());
            throw new RuntimeException("无效的授权请求");
        }

        try {
            // 1. 使用授权码获取访问令牌
            String accessToken = getAccessToken(request.getCode());
            
            // 2. 使用访问令牌获取用户信息
            OAuth2UserInfo userInfo = getUserInfo(accessToken);
            
            // 3. 映射或创建系统B用户
            User user = mapOrCreateUser(userInfo);
            
            // 4. 获取用户权限
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            
            // 5. 生成JWT - 使用业务租户ID而不是数据库主键ID
            String businessTenantId = tenantService.getBusinessTenantIdByPrimaryKey(user.getTenantId());
            String jwt = jwtTokenService.generateToken(user, userDetails, businessTenantId);
            long expiresIn = jwtConfig.getExpiration();
            long expiresAt = System.currentTimeMillis() + expiresIn * 1000;

            log.info("SSO登录成功，用户: {}", user.getUsername());

            return LoginResponse.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .userId(user.getId())
                    .tenantId(user.getTenantId())
                    .loginMethod("sso")
                    .expiresIn(expiresIn)
                    .expiresAt(expiresAt)
                    .build();

        } catch (Exception e) {
            log.error("处理OAuth2回调失败", e);
            throw new RuntimeException("SSO登录失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateState(String state) {
        if (state == null || state.isEmpty()) {
            return false;
        }

        String stateKey = "oauth2:state:" + state;
        String storedState = redisTemplate.opsForValue().get(stateKey);
        
        if ("valid".equals(storedState)) {
            // 删除已使用的状态参数
            redisTemplate.delete(stateKey);
            return true;
        }
        
        return false;
    }

    /**
     * 使用授权码获取访问令牌
     */
    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
            
            log.info("Token endpoint响应状态: {}", response.getStatusCode());
            if (log.isDebugEnabled() && response.getBody() != null) {
                Map<String, Object> safeBody = new LinkedHashMap<>();
                response.getBody().forEach((k, v) -> safeBody.put(String.valueOf(k), v));
                maskTokenField(safeBody, "access_token");
                maskTokenField(safeBody, "refresh_token");
                maskTokenField(safeBody, "id_token");
                log.debug("Token endpoint响应体(已脱敏): {}", safeBody);
            }
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String accessToken = (String) body.get("access_token");
                
                if (accessToken != null && !accessToken.isEmpty()) {
                    log.info("成功获取访问令牌");
                    return accessToken;
                } else {
                    log.error("获取访问令牌失败，access_token为空或不存在");
                    throw new RuntimeException("获取访问令牌失败：access_token为空");
                }
            }
            
            log.error("获取访问令牌失败，响应状态: {}", response.getStatusCode());
            throw new RuntimeException("获取访问令牌失败");
            
        } catch (Exception e) {
            log.error("请求访问令牌异常", e);
            throw new RuntimeException("获取访问令牌失败: " + e.getMessage());
        }
    }

    /**
     * 使用访问令牌获取用户信息
     */
    private OAuth2UserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUri, HttpMethod.GET, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userInfoMap = response.getBody();
                
                OAuth2UserInfo userInfo = objectMapper.convertValue(userInfoMap, OAuth2UserInfo.class);
                log.info("成功获取用户信息: {}", userInfo.getPreferredUsername());
                return userInfo;
            }
            
            log.error("获取用户信息失败，响应状态: {}", response.getStatusCode());
            throw new RuntimeException("获取用户信息失败");
            
        } catch (Exception e) {
            log.error("请求用户信息异常", e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 映射或创建系统B用户
     */
    private User mapOrCreateUser(OAuth2UserInfo userInfo) {
        String username = userInfo.getPreferredUsername();
        
        // 查找现有用户
        User existingUser = userMapper.findByUsername(username);
        
        if (existingUser != null) {
            log.info("找到现有用户: {}, 更新用户信息", username);
            // 更新用户信息（同步系统A的最新数据）
            updateUserFromSSOInfo(existingUser, userInfo);
            if (ensureLocalPassword(existingUser)) {
                log.info("为SSO用户 {} 生成了随机本地密码", existingUser.getUsername());
            }
            userMapper.updateById(existingUser);
            
            // 检查现有用户是否有角色，如果没有则分配默认角色
            ensureUserHasRoles(existingUser);
            
            return existingUser;
        }

        // 如果不允许自动创建用户，抛出异常
        if (!autoCreateUser) {
            log.error("用户不存在且不允许自动创建: {}", username);
            throw new RuntimeException("用户不存在: " + username);
        }

        // 创建新用户
        log.info("创建新用户: {}, 来源: 系统A SSO", username);
        User newUser = createUserFromSSOInfo(userInfo);
        
        try {
            userMapper.insert(newUser);
            log.info("成功创建SSO用户: {}", username);
            
            // 分配默认角色
            assignDefaultRoles(newUser);
            
            return newUser;
        } catch (Exception e) {
            log.error("创建用户失败: {}", username, e);
            throw new RuntimeException("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 确保用户拥有角色，如果没有则分配默认角色
     */
    private void ensureUserHasRoles(User user) {
        try {
            // 检查用户是否已有角色
            List<Long> existingRoleIds = userRoleMapper.findRoleIdsByUserId(user.getId());
            
            if (existingRoleIds != null && !existingRoleIds.isEmpty()) {
                log.info("用户 {} 已有 {} 个角色，无需分配默认角色", user.getUsername(), existingRoleIds.size());
                return;
            }
            
            log.info("用户 {} 没有角色，开始分配默认角色", user.getUsername());
            assignDefaultRoles(user);
            
        } catch (Exception e) {
            log.error("检查用户 {} 角色时发生异常", user.getUsername(), e);
        }
    }
    
    /**
     * 为新用户分配默认角色
     */
    private void assignDefaultRoles(User user) {
        if (defaultRoleIds == null || defaultRoleIds.trim().isEmpty()) {
            log.warn("未配置默认角色ID，跳过为用户 {} 分配角色", user.getUsername());
            return;
        }
        
        try {
            // 解析默认角色ID列表 "2" 或 "2,3,4"
            String[] roleIdArray = defaultRoleIds.split(",");
            List<Long> roleIds = new ArrayList<>();
            
            for (String roleIdStr : roleIdArray) {
                roleIdStr = roleIdStr.trim();
                if (!roleIdStr.isEmpty()) {
                    roleIds.add(Long.parseLong(roleIdStr));
                }
            }
            
            if (roleIds.isEmpty()) {
                log.warn("解析默认角色ID失败，跳过为用户 {} 分配角色", user.getUsername());
                return;
            }
            
            // 为用户分配每个默认角色
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                
                try {
                    userRoleMapper.insert(userRole);
                    log.info("成功为用户 {} 分配角色 ID: {}", user.getUsername(), roleId);
                } catch (Exception e) {
                    // 可能是重复分配，记录警告但不中断流程
                    log.warn("为用户 {} 分配角色 ID: {} 失败: {}", user.getUsername(), roleId, e.getMessage());
                }
            }
            
            log.info("完成为SSO用户 {} 分配 {} 个默认角色", user.getUsername(), roleIds.size());
            
        } catch (Exception e) {
            log.error("为用户 {} 分配默认角色时发生异常", user.getUsername(), e);
            // 不抛出异常，避免影响用户创建流程
        }
    }
    
    /**
     * 从SSO信息创建新用户
     */
    private User createUserFromSSOInfo(OAuth2UserInfo userInfo) {
        User newUser = new User();
        
        // 基础信息
        newUser.setUsername(userInfo.getPreferredUsername());
        // 为防止后续本地登录/密码校验，SSO用户仍需持久化一个加密密码
        ensureLocalPassword(newUser);
        newUser.setStatus(userInfo.getStatus() != null ? userInfo.getStatus() : 1); // 默认启用
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        
        // 租户映射：系统A的tenant_id -> 系统B的租户主键ID
        Long tenantId = mapTenantId(userInfo.getTenantId());
        newUser.setTenantId(tenantId);
        
        // 设置用户详细信息
        updateUserDetailsFromSSO(newUser, userInfo);
        
        return newUser;
    }

    /**
     * 确保用户拥有可用于本地认证的加密口令。
     */
    private boolean ensureLocalPassword(User user) {
        if (user == null) {
            return false;
        }
        if (StringUtils.hasText(user.getPassword())) {
            return false;
        }
        String randomSecret = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(randomSecret));
        userDetailsCacheHelper.evictUser(user);
        return true;
    }
    
    /**
     * 从SSO信息更新现有用户
     */
    private void updateUserFromSSOInfo(User existingUser, OAuth2UserInfo userInfo) {
        // 更新时间戳
        existingUser.setUpdateTime(LocalDateTime.now());
        
        // 同步用户状态（如果系统A禁用了用户，系统B也应该更新状态）
        if (userInfo.getStatus() != null) {
            existingUser.setStatus(userInfo.getStatus());
        }
        
        // 更新用户详细信息
        updateUserDetailsFromSSO(existingUser, userInfo);
        
        log.info("用户 {} 信息已从系统A同步更新", existingUser.getUsername());
    }
    
    /**
     * 更新用户详细信息
     */
    private void updateUserDetailsFromSSO(User user, OAuth2UserInfo userInfo) {
        // 设置用户详细信息
        // 避免以 info 级别输出姓名/邮箱/手机等PII；如需排查问题请在 debug 下查看脱敏信息
        if (StringUtils.hasText(userInfo.getName())) {
            log.debug("用户 {} 同步了姓名信息", user.getUsername());
        }

        if (StringUtils.hasText(userInfo.getEmail())) {
            log.debug("用户 {} 邮箱(脱敏): {}", user.getUsername(), maskEmail(userInfo.getEmail()));
        }

        if (StringUtils.hasText(userInfo.getPhone())) {
            log.debug("用户 {} 手机(脱敏): {}", user.getUsername(), maskPhone(userInfo.getPhone()));
        }
        
        // 4A用户特殊处理
        if ("1".equals(userInfo.getIs4A())) {
            // 避免输出4A用户的标识信息
            log.debug("用户 {} 是4A用户", user.getUsername());
            // 可以在这里添加4A用户的特殊处理逻辑
        }
        
        // 角色信息处理
        if (userInfo.getRoleId() != null) {
            log.debug("用户 {} 同步了系统A角色信息", user.getUsername());
            // 这里可以实现角色映射逻辑，将系统A的角色映射到系统B的角色
        }
        
        // 部门和岗位信息
        if (userInfo.getDeptId() != null) {
            log.debug("用户 {} 同步了系统A部门信息", user.getUsername());
        }
        
        if (userInfo.getPostId() != null) {
            log.debug("用户 {} 同步了系统A岗位信息", user.getUsername());
        }
    }

    private static void maskTokenField(Map<String, Object> body, String field) {
        if (body == null || field == null) {
            return;
        }
        if (body.containsKey(field) && body.get(field) != null) {
            body.put(field, "***");
        }
    }

    private static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        return local.charAt(0) + "***" + domain;
    }

    private static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        String digits = phone.trim();
        if (digits.length() <= 4) {
            return "***";
        }
        String prefix = digits.substring(0, Math.min(3, digits.length()));
        String suffix = digits.substring(digits.length() - 2);
        return prefix + "****" + suffix;
    }
    
    /**
     * 映射租户ID：系统A的字符串tenant_id -> 系统B的数字租户主键ID
     */
    private Long mapTenantId(String systemATenantId) {
        if (systemATenantId == null || systemATenantId.trim().isEmpty()) {
            return defaultTenantId; // 使用默认租户ID
        }
        
        // 根据系统A的tenant_id查找系统B对应的租户主键ID
        try {
            // 如果系统A和系统B使用相同的租户编码规则
            if ("000000".equals(systemATenantId)) {
                // 默认租户映射到ID=1
                return 1L;
            }
            
            // 可以扩展更多租户映射逻辑
            // 例如：通过tenantService.getByBusinessTenantId(systemATenantId)来查找
            log.warn("未找到系统A租户 {} 的映射，使用默认租户", systemATenantId);
            return defaultTenantId;
            
        } catch (Exception e) {
            log.error("租户ID映射失败，系统A租户ID: {}, 使用默认租户", systemATenantId, e);
            return defaultTenantId;
        }
    }
}
