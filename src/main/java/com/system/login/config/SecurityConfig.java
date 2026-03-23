package com.system.login.config;

import com.system.ai.security.AiApiKeyAuthenticationFilter;
import com.system.ai.security.AiRateLimitFilter;
import com.system.login.security.JwtAuthenticationFilter;
import com.system.login.security.tenant.TenantFilter;
import com.system.login.security.TokenStatusFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.core.annotation.Order;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantFilter tenantFilter;
    private final TokenStatusFilter tokenStatusFilter;
    private final AiApiKeyAuthenticationFilter aiApiKeyAuthenticationFilter;
    private final AiRateLimitFilter aiRateLimitFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          TenantFilter tenantFilter,
                          TokenStatusFilter tokenStatusFilter,
                          AiApiKeyAuthenticationFilter aiApiKeyAuthenticationFilter,
                          AiRateLimitFilter aiRateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantFilter = tenantFilter;
        this.tokenStatusFilter = tokenStatusFilter;
        this.aiApiKeyAuthenticationFilter = aiApiKeyAuthenticationFilter;
        this.aiRateLimitFilter = aiRateLimitFilter;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 允许异步/错误分派，避免 WebAsyncTask 二次分派被误判为未认证
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        // 登录接口不需要认证
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        // SSO相关接口不需要认证
                        .requestMatchers("/api/auth/sso/**").permitAll()
                        .requestMatchers("/api/auth/oauth2/**").permitAll()
                        // 健康检查接口不需要认证
                        .requestMatchers("/api/health").permitAll()
                        // Swagger/OpenAPI文档不需要认证
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 其他所有接口都需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class)
                // 添加租户过滤器
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
                // 附加令牌状态信息
                .addFilterAfter(tokenStatusFilter, JwtAuthenticationFilter.class)
                // AI接口支持内部API Key鉴权（无JWT场景）
                .addFilterAfter(aiApiKeyAuthenticationFilter, JwtAuthenticationFilter.class)
                // AI接口防滥用（限流/审计）
                .addFilterAfter(aiRateLimitFilter, AiApiKeyAuthenticationFilter.class)
                // 添加JWT认证过滤器，确保在AnonymousAuthenticationFilter之前执行

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
