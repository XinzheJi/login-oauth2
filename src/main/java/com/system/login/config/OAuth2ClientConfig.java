package com.system.login.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * OAuth2 授权服务器配置
 * 用于配置系统B作为OAuth2客户端，支持授权码模式的SSO登录
 */
@Configuration
public class OAuth2ClientConfig {

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

    /**
     * 配置OAuth2客户端注册信息
     * 用于支持授权码模式的SSO登录
     */
    @Bean
    public ClientRegistrationRepository oauth2ClientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId("system-a-sso")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName("sub")
                .scope("openid", "profile", "email")
                .clientName("System A SSO")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}
