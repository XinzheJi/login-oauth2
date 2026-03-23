package com.system.login.security.debug;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Import Ordered and Order to control filter order
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 21)
@Slf4j
public class CheckSecurityContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Check the SecurityContextHolder state
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            log.info("CheckSecurityContextFilter: SecurityContextHolder contains authenticated user: {}", authentication.getName());
        } else {
            log.warn("CheckSecurityContextFilter: SecurityContextHolder is null or contains anonymous/unauthenticated user for request: {}", request.getRequestURI());
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
