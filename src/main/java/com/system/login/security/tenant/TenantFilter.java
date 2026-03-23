package com.system.login.security.tenant;

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

/**
 * 租户过滤器，从JWT令牌中提取租户ID并设置到租户上下文中
 * 注意：该过滤器需要在JWT认证过滤器之前执行
 */
@Slf4j
@Component
//@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantFilter extends OncePerRequestFilter {
    private final JwtConfig jwtConfig;
    private final JwtTokenService jwtTokenService;

    @Autowired
    public TenantFilter(JwtConfig jwtConfig, JwtTokenService jwtTokenService) {
        this.jwtConfig = jwtConfig;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中获取JWT令牌
            String jwt = parseJwt(request);
            if (jwt != null) {
                // 从JWT令牌中获取租户ID (修改为 String)
                String tenantId = jwtTokenService.getTenantIdFromToken(jwt);
                if (tenantId != null) {
                    // 设置租户ID到上下文中
                    TenantContext.setCurrentTenant(tenantId);
                    log.debug("设置当前租户ID: {}", tenantId);
                }
            }
        } catch (Exception e) {
            log.error("提取租户ID失败", e);
            // 出现异常不影响请求继续处理
        }
        try {
            // 继续执行过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清除租户上下文
            TenantContext.clear();
        }
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