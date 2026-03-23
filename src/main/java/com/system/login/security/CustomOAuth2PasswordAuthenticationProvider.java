package com.system.login.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

import java.util.Collections;

/**
 * 自定义密码认证提供者，用于替代已被移除的OAuth2PasswordAuthenticationProvider
 */
@Slf4j
public class CustomOAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2PasswordAuthenticationProvider(UserDetailsService userDetailsService, 
                                                     PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 确保只处理用户名密码认证请求
        if (!supports(authentication.getClass())) {
            return null;
        }

        UsernamePasswordAuthenticationToken usernamePasswordToken = (UsernamePasswordAuthenticationToken) authentication;
        String username = usernamePasswordToken.getName();
        String password = usernamePasswordToken.getCredentials().toString();

        log.debug("尝试使用密码模式认证用户: {}", username);

        try {
            // 获取用户详细信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 验证密码
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                log.debug("用户 {} 密码验证成功", username);
                
                // 返回认证成功的令牌
                return new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        password, 
                        userDetails.getAuthorities());
            } else {
                log.warn("用户 {} 密码验证失败", username);
                throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT), "用户名或密码错误");
            }
        } catch (Exception ex) {
            log.error("用户 {} 认证失败: {}", username, ex.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT), ex.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}