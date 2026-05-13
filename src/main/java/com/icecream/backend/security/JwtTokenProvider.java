package com.icecream.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT令牌提供者类
 * 负责JWT令牌的生成、验证和解析
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:1800000}") // 30分钟，单位毫秒
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7天，单位毫秒
    private long refreshTokenExpiration;

    @Value("${jwt.issuer:icecream-backend}")
    private String issuer;

    /**
     * 根据用户名和角色生成访问令牌
     *
     * @param username 用户名
     * @param role 用户角色
     * @return JWT访问令牌
     */
    public String generateAccessToken(String username, String role) {
        log.debug("生成访问令牌: username={}, role={}", username, role);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 根据用户名生成刷新令牌
     *
     * @param username 用户名
     * @return JWT刷新令牌
     */
    public String generateRefreshToken(String username) {
        log.debug("生成刷新令牌: username={}", username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 根据Spring Security的Authentication对象生成访问令牌
     *
     * @param authentication Spring Security认证对象
     * @return JWT访问令牌
     */
    public String generateAccessToken(Authentication authentication) {
        log.debug("根据Authentication生成访问令牌: username={}", authentication.getName());

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(authentication.getName())
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从令牌中提取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从令牌中提取角色
     *
     * @param token JWT令牌
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * 从令牌中提取令牌类型（access或refresh）
     *
     * @param token JWT令牌
     * @return 令牌类型
     */
    public String getTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * 从令牌中提取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 验证令牌是否有效（不检查用户详情）
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (MalformedJwtException e) {
            log.error("无效的JWT令牌: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌声明为空: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 从令牌中提取指定声明
     *
     * @param token JWT令牌
     * @param claimsResolver 声明解析函数
     * @param <T> 声明类型
     * @return 声明值
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中提取所有声明
     *
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT密钥长度不足（当前" + (jwtSecret == null ? 0 : jwtSecret.length())
                + "字符），生产环境必须通过JWT_SECRET环境变量设置至少32字符的强密钥");
        }
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取访问令牌过期时间（毫秒）
     *
     * @return 访问令牌过期时间
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * 获取刷新令牌过期时间（毫秒）
     *
     * @return 刷新令牌过期时间
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}