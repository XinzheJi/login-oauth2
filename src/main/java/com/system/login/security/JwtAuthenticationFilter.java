package com.system.login.security;

import com.system.login.config.JwtConfig;
import com.system.login.service.security.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 10) // 确保在CheckSecurityContextFilter之前执行
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtConfig jwtConfig;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtConfig jwtConfig, JwtTokenService jwtTokenService, UserDetailsService userDetailsService) {
        this.jwtConfig = jwtConfig;
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = parseJwt(request);
        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = null;
        try {
            username = jwtTokenService.getUsernameFromToken(jwt);
        } catch (Exception ex) {
            log.warn("解析JWT令牌失败: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (!StringUtils.hasText(username)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtTokenService.validateToken(jwt, userDetails)) {
                String authoritiesString = jwtTokenService.getAuthoritiesFromToken(jwt);
                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                if (StringUtils.hasText(authoritiesString)) {
                    authorities = Arrays.stream(authoritiesString.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (UsernameNotFoundException ex) {
            log.warn("JWT用户不存在: {}", username);
        } catch (Exception ex) {
            log.error("JWT认证失败", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中解析JWT令牌
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(jwtConfig.getTokenHeader());
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(jwtConfig.getTokenPrefix() + " ")) {
            return headerAuth.substring(jwtConfig.getTokenPrefix().length() + 1);
        }
        return null;
    }

    // Keep the @Order annotation at the class level
    // No need to override getOrder() method
}