package com.system.ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * AI接口 API Key 鉴权：用于无JWT场景的服务间调用（例如内部网关/大屏）。
 *
 * <p>仅对 /api/ai/** 生效；当请求已通过 JWT 认证时不重复处理。</p>
 */
@Slf4j
@Component
public class AiApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final String headerName;
    private final byte[] expectedKey;

    public AiApiKeyAuthenticationFilter(
            @Value("${ai.api-key.enabled:true}") boolean enabled,
            @Value("${ai.api-key.header:X-AI-API-KEY}") String headerName,
            @Value("${ai.api-key.value:}") String apiKeyValue
    ) {
        this.headerName = StringUtils.hasText(headerName) ? headerName : "X-AI-API-KEY";
        boolean hasKey = StringUtils.hasText(apiKeyValue);
        this.enabled = enabled && hasKey;
        this.expectedKey = hasKey ? apiKeyValue.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        String uri = request != null ? request.getRequestURI() : null;
        return uri == null || !uri.startsWith("/api/ai/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String provided = extractKey(request);
        if (!StringUtils.hasText(provided)) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedKey, providedBytes)) {
            log.warn("AI接口API Key校验失败: ip={}, uri={}", getClientIp(request), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "ai-api-key",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_AI_API"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private String extractKey(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String direct = request.getHeader(headerName);
        if (StringUtils.hasText(direct)) {
            return direct.trim();
        }

        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.toLowerCase().startsWith("bearer ")) {
            return authorization.substring(7).trim();
        }

        return null;
    }

    private static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}

