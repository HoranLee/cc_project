package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 —— 负责 Token 的生成、验证和解析。
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;
    private final long rememberMeExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration,
                   @Value("${jwt.remember-me-expiration}") long rememberMeExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.rememberMeExpiration = rememberMeExpiration;
    }

    /**
     * 生成 JWT Token。
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param rememberMe 是否记住我（影响过期时间）
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username, boolean rememberMe) {
        long now = System.currentTimeMillis();
        long expiry = now + (rememberMe ? rememberMeExpiration : expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date(now))
                .expiration(new Date(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 中解析用户ID。
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从 Token 中解析用户名。
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证 Token 是否有效。
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析 Token 获取 Claims。
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
