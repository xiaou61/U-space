package com.xiaou.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * 通用JWT工具类
 *
 * @author xiaou
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:defaultSecretKeyForJwtToken123456789}")
    private String secret;

    @Value("${jwt.expiration:7200}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:86400}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(String username, Long userId, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("userType", userType) // admin 或 user
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String username, Long userId, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration * 1000);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("userType", userType)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            log.error("从Token中获取用户名失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Object userIdObj = claims.get("userId");
                if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("从Token中获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取用户类型
     */
    public String getUserTypeFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? (String) claims.get("userType") : null;
        } catch (Exception e) {
            log.error("从Token中获取用户类型失败", e);
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token已过期: {}", token);
        } catch (UnsupportedJwtException e) {
            log.error("不支持的Token: {}", token);
        } catch (MalformedJwtException e) {
            log.error("Token格式错误: {}", token);
        } catch (Exception e) {
            log.error("Token验证失败: {}", token, e);
        }
        return false;
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Date expiration = claims.getExpiration();
                return expiration.before(new Date());
            }
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("检查Token过期状态失败", e);
        }
        return true;
    }

    /**
     * 从Token中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从请求头中获取Token
     */
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 获取Token的剩余有效时间（秒）
     */
    public Long getTokenRemainingTime(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims != null) {
                Date expiration = claims.getExpiration();
                long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                return Math.max(0, remaining);
            }
        } catch (Exception e) {
            log.error("获取Token剩余时间失败", e);
        }
        return 0L;
    }

    // ============= 兼容 system 模块的方法 =============

    /**
     * 生成访问令牌 (兼容 system 模块)
     */
    public String generateAccessToken(String username, Long userId) {
        return generateAccessToken(username, userId, "admin");
    }

    /**
     * 生成刷新令牌 (兼容 system 模块)
     */
    public String generateRefreshToken(String username, Long userId) {
        return generateRefreshToken(username, userId, "admin");
    }

    /**
     * 验证令牌 (兼容 system 模块)
     */
    public Boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return (username.equals(tokenUsername) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("令牌验证失败", e);
            return false;
        }
    }

    /**
     * 刷新令牌 (兼容 system 模块)
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            Long userId = getUserIdFromToken(token);
            return generateAccessToken(username, userId);
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("获取过期时间失败", e);
            return null;
        }
    }
} 