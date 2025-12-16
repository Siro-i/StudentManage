package com.school.system.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类，用于生成和解析 JWT 令牌
 *
 *
 */
public class JwtUtils {

    // 1. 生成一个安全的密钥 (HMAC-SHA256)
    private static final SecretKey KEY = Jwts.SIG.HS256.key().build();

    // 过期时间 12小时
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 12;

    /**
     * 生成 Token
     *
     * @param userId   用户 ID
     * @param userName 用户名
     * @param userType 角色
     * @return JWT 字符串
     */
    public static String generateToken(Long userId, String userName, String userType) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .subject(userName)
                .claim("userId", userId)
                .claim("userType", userType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }
    /**
     * 解析 Token 获取 Claims (载荷)
     * 如果 Token 无效或过期，这里会抛出异常
     *
     * @param token JWT 字符串
     * @return 载荷 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}