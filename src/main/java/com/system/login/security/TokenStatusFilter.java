package com.system.login.security;

import com.system.login.config.JwtConfig;
import com.system.login.service.security.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

/**
 * 将令牌状态信息添加到API响应头
 */
@Slf4j
@Component
public class TokenStatusFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public TokenStatusFilter(JwtConfig jwtConfig, JwtTokenService jwtTokenService) {
        this.jwtConfig = jwtConfig;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String headerAuth = request.getHeader(jwtConfig.getTokenHeader());
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(jwtConfig.getTokenPrefix() + " ")) {
            String token = headerAuth.substring(jwtConfig.getTokenPrefix().length() + 1);
            try {
                Date expiration = jwtTokenService.getExpirationDateFromToken(token);
                boolean expired = jwtTokenService.isTokenExpired(token);
                long remainingSeconds = expired ? 0 : jwtTokenService.getRemainingSeconds(token);

                response.setHeader("X-Token-Expired", String.valueOf(expired));
                response.setHeader("X-Token-Expires-At", String.valueOf(expiration.getTime()));
                response.setHeader("X-Token-Remaining-Seconds", String.valueOf(remainingSeconds));
            } catch (Exception e) {
                log.debug("附加令牌状态失败: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
