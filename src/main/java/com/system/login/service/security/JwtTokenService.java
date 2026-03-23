package com.system.login.service.security;

import com.system.login.config.JwtConfig;
import com.system.login.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Token服务，用于生成和验证JWT令牌
 */
@Slf4j
@Service
public class JwtTokenService {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    
    /**
     * 租户ID的声明名称
     */
    private static final String CLAIM_TENANT_ID = "tenant_id";
    
    /**
     * 权限的声明名称
     */
    private static final String CLAIM_AUTHORITIES = "authorities";
    
    @Autowired
    public JwtTokenService(JwtConfig jwtConfig, SecretKey jwtSecretKey) {
        this.jwtConfig = jwtConfig;
        this.secretKey = jwtSecretKey;
    }
    
    /**
     * 从用户信息生成JWT令牌
     */
    public String generateToken(User user, UserDetails userDetails, String businessTenantId) {
        Map<String, Object> claims = new HashMap<>();
        // 添加租户ID (业务ID)
        claims.put(CLAIM_TENANT_ID, businessTenantId);
        
        // 添加用户权限
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put(CLAIM_AUTHORITIES, authorities);
        
        return createToken(claims, userDetails.getUsername());
    }
    
    /**
     * 创建JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtConfig.getExpiration() * 1000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 验证JWT令牌
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    /**
     * 从JWT令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    /**
     * 从JWT令牌中获取租户ID
     */
    public String getTenantIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(CLAIM_TENANT_ID, String.class);
    }
    
    /**
     * 从JWT令牌中获取权限
     */
    public String getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(CLAIM_AUTHORITIES, String.class);
    }
    
    /**
     * 从JWT令牌中获取所有声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 从JWT令牌中获取过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }
    
    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 获取令牌剩余有效期（秒）
     */
    public long getRemainingSeconds(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long diff = expiration.getTime() - System.currentTimeMillis();
        return diff <= 0 ? 0 : diff / 1000;
    }
}
