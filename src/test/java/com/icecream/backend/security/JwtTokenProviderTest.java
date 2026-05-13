package com.icecream.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT令牌提供者测试类
 * 测试JWT令牌的生成、验证和解析功能
 */
@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void testGenerateAndValidateAccessToken() {
        // 测试数据
        String username = "testuser";
        String role = "ROLE_USER";

        // 生成访问令牌
        String accessToken = jwtTokenProvider.generateAccessToken(username, role);
        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());

        // 验证令牌
        assertTrue(jwtTokenProvider.validateToken(accessToken));

        // 从令牌中提取信息
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(accessToken);
        String extractedRole = jwtTokenProvider.getRoleFromToken(accessToken);
        String tokenType = jwtTokenProvider.getTokenType(accessToken);

        assertEquals(username, extractedUsername);
        assertEquals(role, extractedRole);
        assertEquals("access", tokenType);
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        // 测试数据
        String username = "testuser";

        // 生成刷新令牌
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        // 验证令牌
        assertTrue(jwtTokenProvider.validateToken(refreshToken));

        // 从令牌中提取信息
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);

        assertEquals(username, extractedUsername);
        assertEquals("refresh", tokenType);
    }

    @Test
    void testInvalidToken() {
        // 测试无效令牌
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void testTokenExpiration() {
        // 测试令牌过期时间
        String username = "testuser";
        String role = "ROLE_USER";

        String token = jwtTokenProvider.generateAccessToken(username, role);
        assertNotNull(jwtTokenProvider.getExpirationDateFromToken(token));
    }

    @Test
    void testDifferentUsersGenerateDifferentTokens() {
        // 测试不同用户生成不同的令牌
        String user1Token = jwtTokenProvider.generateAccessToken("user1", "ROLE_USER");
        String user2Token = jwtTokenProvider.generateAccessToken("user2", "ROLE_ADMIN");

        assertNotEquals(user1Token, user2Token);

        // 验证两个令牌都有效
        assertTrue(jwtTokenProvider.validateToken(user1Token));
        assertTrue(jwtTokenProvider.validateToken(user2Token));

        // 验证用户信息
        assertEquals("user1", jwtTokenProvider.getUsernameFromToken(user1Token));
        assertEquals("ROLE_USER", jwtTokenProvider.getRoleFromToken(user1Token));

        assertEquals("user2", jwtTokenProvider.getUsernameFromToken(user2Token));
        assertEquals("ROLE_ADMIN", jwtTokenProvider.getRoleFromToken(user2Token));
    }
}