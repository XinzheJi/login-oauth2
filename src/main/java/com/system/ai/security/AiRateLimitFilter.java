package com.system.ai.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.system.login.domain.vo.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * AI接口防滥用过滤器：对 /api/ai/** 进行简单限流与访问审计。
 */
@Slf4j
@Component
public class AiRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000L;

    private final boolean enabled;
    private final int requestsPerMinute;
    private final Cache<String, FixedWindowCounter> counters;

    public AiRateLimitFilter(
            @Value("${ai.rate-limit.enabled:true}") boolean enabled,
            @Value("${ai.rate-limit.requests-per-minute:60}") int requestsPerMinute,
            @Value("${ai.rate-limit.cache-expire-minutes:10}") int cacheExpireMinutes
    ) {
        this.enabled = enabled;
        this.requestsPerMinute = Math.max(1, requestsPerMinute);
        this.counters = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(Math.max(1, cacheExpireMinutes)))
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request != null ? request.getRequestURI() : null;
        return uri == null || !uri.startsWith("/api/ai/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = buildRateLimitKey(request);
        long now = System.currentTimeMillis();
        FixedWindowCounter counter = counters.get(key, k -> new FixedWindowCounter(now));
        if (!counter.tryAcquire(now, requestsPerMinute)) {
            log.warn("AI接口触发限流: key={}, ip={}, uri={}", key, getClientIp(request), request.getRequestURI());
            writeJson(response, HttpStatus.TOO_MANY_REQUESTS, ApiResponse.error("请求过于频繁，请稍后再试"));
            return;
        }

        long startNs = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long costMs = (System.nanoTime() - startNs) / 1_000_000;
            log.info("AI接口访问审计: user={}, ip={}, method={}, uri={}, status={}, costMs={}",
                    getUsername(), getClientIp(request), request.getMethod(), request.getRequestURI(), response.getStatus(), costMs);
        }
    }

    private static void writeJson(HttpServletResponse response, HttpStatus status, ApiResponse<?> body) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(toJson(body));
    }

    private static String toJson(ApiResponse<?> body) {
        if (body == null) {
            return "{}";
        }
        String err = body.getErrormsg() == null ? "" : escapeJson(body.getErrormsg());
        String state = body.getState() != null && body.getState() ? "true" : "false";
        return "{\"state\":" + state + ",\"errormsg\":\"" + err + "\"}";
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String buildRateLimitKey(HttpServletRequest request) {
        String username = getUsername();
        if (StringUtils.hasText(username) && !"anonymousUser".equalsIgnoreCase(username)) {
            return "u:" + username;
        }
        return "ip:" + getClientIp(request);
    }

    private static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        String name = authentication.getName();
        return StringUtils.hasText(name) ? name : "anonymous";
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

    private static final class FixedWindowCounter {
        private long windowStartMs;
        private int count;

        private FixedWindowCounter(long nowMs) {
            this.windowStartMs = nowMs;
        }

        private synchronized boolean tryAcquire(long nowMs, int limit) {
            if (nowMs - windowStartMs >= WINDOW_MS) {
                windowStartMs = nowMs;
                count = 0;
            }
            count++;
            return count <= limit;
        }
    }
}

