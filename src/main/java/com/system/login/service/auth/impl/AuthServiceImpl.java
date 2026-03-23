package com.system.login.service.auth.impl;

import com.system.login.domain.entity.User;
import com.system.login.domain.vo.LoginRequest;
import com.system.login.domain.vo.LoginResponse;
import com.system.login.mapper.UserMapper;
import com.system.login.service.auth.AuthService;
import com.system.login.service.security.JwtTokenService;
import com.system.login.service.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final TenantService tenantService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                          JwtTokenService jwtTokenService,
                          UserMapper userMapper,
                          TenantService tenantService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userMapper = userMapper;
        this.tenantService = tenantService;
    }
    
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());
        
        // 调用Spring Security进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userMapper.findByUsername(userDetails.getUsername());

        String businessTenantId = tenantService.getBusinessTenantIdByPrimaryKey(user.getTenantId());
        if (businessTenantId == null) {
            log.error("无法确定用户 {} 的业务租户ID，登录失败", user.getUsername());
            throw new RuntimeException("用户租户信息配置错误");
        }

        String jwt = jwtTokenService.generateToken(user, userDetails, businessTenantId);

//        System.out.println("JWT令牌: " + jwt);
        
        // 返回登录响应
        return LoginResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .username(user.getUsername())
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .loginMethod("local")
                .build();
    }
}