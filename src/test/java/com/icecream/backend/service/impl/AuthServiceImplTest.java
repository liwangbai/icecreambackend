package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.LoginRequest;
import com.icecream.backend.dto.request.RefreshTokenRequest;
import com.icecream.backend.dto.request.RegisterRequest;
import com.icecream.backend.dto.response.AuthResponse;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.User;
import com.icecream.backend.security.JwtTokenProvider;
import com.icecream.backend.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试类
 * 测试AuthServiceImpl的各种认证场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setNickname("Test User");
        testUser.setRole("ROLE_USER");
        testUser.setStatus(1); // 启用状态

        // 初始化注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("New User");

        // 初始化登录请求
        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        // 初始化刷新令牌请求
        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-token");
    }

    @Nested
    @DisplayName("用户注册测试")
    class RegisterTests {

        @Test
        @DisplayName("成功注册新用户")
        void testRegister_Success() {
            // Arrange
            when(userMapper.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(2L); // 模拟数据库生成ID
                return 1;
            });
            when(jwtTokenProvider.generateAccessToken("newuser", "ROLE_USER")).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken("newuser")).thenReturn("refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L); // 1小时

            // Act
            AuthResponse response = authService.register(registerRequest);

            // Assert
            assertNotNull(response);
            assertEquals(2L, response.getUserId());
            assertEquals("newuser", response.getUsername());
            assertEquals("New User", response.getNickname());
            assertEquals("ROLE_USER", response.getRole());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());
            assertEquals(3600L, response.getExpiresIn()); // 转换为秒

            // 验证方法调用
            verify(userMapper).findByUsername("newuser");
            verify(userMapper).findByEmail("newuser@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userMapper).insert(any(User.class));
            verify(jwtTokenProvider).generateAccessToken("newuser", "ROLE_USER");
            verify(jwtTokenProvider).generateRefreshToken("newuser");
        }

        @Test
        @DisplayName("注册时用户名已存在")
        void testRegister_UsernameAlreadyExists() {
            // Arrange
            when(userMapper.findByUsername("newuser")).thenReturn(Optional.of(testUser));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.register(registerRequest));

            assertEquals("用户名已存在: newuser", exception.getMessage());
            verify(userMapper).findByUsername("newuser");
            verify(userMapper, never()).findByEmail(anyString());
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("注册时邮箱已存在")
        void testRegister_EmailAlreadyExists() {
            // Arrange
            when(userMapper.findByUsername("newuser")).thenReturn(Optional.empty());
            when(userMapper.findByEmail("newuser@example.com")).thenReturn(Optional.of(testUser));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.register(registerRequest));

            assertEquals("邮箱已存在: newuser@example.com", exception.getMessage());
            verify(userMapper).findByUsername("newuser");
            verify(userMapper).findByEmail("newuser@example.com");
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {

        @Test
        @DisplayName("成功登录")
        void testLogin_Success() {
            // Arrange
            when(userMapper.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(userMapper.updateLastLogin(1L)).thenReturn(1);
            when(jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER")).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

            // Act
            AuthResponse response = authService.login(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getUserId());
            assertEquals("testuser", response.getUsername());
            assertEquals("Test User", response.getNickname());
            assertEquals("ROLE_USER", response.getRole());
            assertEquals("access-token", response.getAccessToken());
            assertEquals("refresh-token", response.getRefreshToken());

            verify(userMapper).findByUsernameOrEmail("testuser");
            verify(passwordEncoder).matches("password123", "encodedPassword");
            verify(userMapper).updateLastLogin(1L);
            verify(jwtTokenProvider).generateAccessToken("testuser", "ROLE_USER");
            verify(jwtTokenProvider).generateRefreshToken("testuser");
        }

        @Test
        @DisplayName("登录时用户不存在")
        void testLogin_UserNotFound() {
            // Arrange
            when(userMapper.findByUsernameOrEmail("testuser")).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());
            verify(userMapper).findByUsernameOrEmail("testuser");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("登录时密码错误")
        void testLogin_WrongPassword() {
            // Arrange
            when(userMapper.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户名或密码错误", exception.getMessage());
            verify(userMapper).findByUsernameOrEmail("testuser");
            verify(passwordEncoder).matches("password123", "encodedPassword");
        }

        @Test
        @DisplayName("登录时用户被禁用")
        void testLogin_UserDisabled() {
            // Arrange
            testUser.setStatus(0); // 禁用状态
            when(userMapper.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest));

            assertEquals("用户已被禁用", exception.getMessage());
            verify(userMapper).findByUsernameOrEmail("testuser");
            verify(passwordEncoder).matches("password123", "encodedPassword");
        }
    }

    @Nested
    @DisplayName("令牌刷新测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("成功刷新令牌")
        void testRefreshToken_Success() {
            // Arrange
            when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
            when(jwtTokenProvider.getTokenType("refresh-token")).thenReturn("refresh");
            when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(false);
            when(jwtTokenProvider.getUsernameFromToken("refresh-token")).thenReturn("testuser");
            when(userMapper.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            doNothing().when(tokenBlacklistService).addToBlacklist(eq("refresh-token"), anyLong());
            when(jwtTokenProvider.generateAccessToken("testuser", "ROLE_USER")).thenReturn("new-access-token");
            when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("new-refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

            // Act
            AuthResponse response = authService.refreshToken(refreshTokenRequest);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getUserId());
            assertEquals("testuser", response.getUsername());
            assertEquals("new-access-token", response.getAccessToken());
            assertEquals("new-refresh-token", response.getRefreshToken());

            verify(jwtTokenProvider).validateToken("refresh-token");
            verify(jwtTokenProvider).getTokenType("refresh-token");
            verify(tokenBlacklistService).isBlacklisted("refresh-token");
            verify(jwtTokenProvider).getUsernameFromToken("refresh-token");
            verify(userMapper).findByUsername("testuser");
            verify(tokenBlacklistService).addToBlacklist(eq("refresh-token"), eq(7 * 24 * 60 * 60L));
            verify(jwtTokenProvider).generateAccessToken("testuser", "ROLE_USER");
            verify(jwtTokenProvider).generateRefreshToken("testuser");
        }

        @Test
        @DisplayName("刷新令牌无效")
        void testRefreshToken_InvalidToken() {
            // Arrange
            when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(refreshTokenRequest));

            assertEquals("刷新令牌无效", exception.getMessage());
            verify(jwtTokenProvider).validateToken("refresh-token");
            verify(jwtTokenProvider, never()).getTokenType(anyString());
        }

        @Test
        @DisplayName("刷新令牌类型错误")
        void testRefreshToken_WrongTokenType() {
            // Arrange
            when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
            when(jwtTokenProvider.getTokenType("refresh-token")).thenReturn("access");

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(refreshTokenRequest));

            assertEquals("令牌类型错误", exception.getMessage());
            verify(jwtTokenProvider).validateToken("refresh-token");
            verify(jwtTokenProvider).getTokenType("refresh-token");
        }

        @Test
        @DisplayName("刷新令牌在黑名单中")
        void testRefreshToken_TokenBlacklisted() {
            // Arrange
            when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
            when(jwtTokenProvider.getTokenType("refresh-token")).thenReturn("refresh");
            when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(refreshTokenRequest));

            assertEquals("刷新令牌已失效", exception.getMessage());
            verify(jwtTokenProvider).validateToken("refresh-token");
            verify(jwtTokenProvider).getTokenType("refresh-token");
            verify(tokenBlacklistService).isBlacklisted("refresh-token");
        }

        @Test
        @DisplayName("刷新令牌对应的用户不存在")
        void testRefreshToken_UserNotFound() {
            // Arrange
            when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
            when(jwtTokenProvider.getTokenType("refresh-token")).thenReturn("refresh");
            when(tokenBlacklistService.isBlacklisted("refresh-token")).thenReturn(false);
            when(jwtTokenProvider.getUsernameFromToken("refresh-token")).thenReturn("nonexistent");
            when(userMapper.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.refreshToken(refreshTokenRequest));

            assertEquals("用户不存在", exception.getMessage());
            verify(jwtTokenProvider).validateToken("refresh-token");
            verify(jwtTokenProvider).getTokenType("refresh-token");
            verify(tokenBlacklistService).isBlacklisted("refresh-token");
            verify(jwtTokenProvider).getUsernameFromToken("refresh-token");
            verify(userMapper).findByUsername("nonexistent");
        }
    }

    @Nested
    @DisplayName("用户登出测试")
    class LogoutTests {

        @Test
        @DisplayName("成功登出")
        void testLogout_Success() {
            // Arrange
            String accessToken = "valid-access-token";
            String refreshToken = "valid-refresh-token";

            when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
            when(jwtTokenProvider.getUsernameFromToken(accessToken)).thenReturn("testuser");
            when(jwtTokenProvider.getTokenType(accessToken)).thenReturn("access");
            when(jwtTokenProvider.getExpirationDateFromToken(accessToken)).thenReturn(new Date(System.currentTimeMillis() + 3600000));

            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtTokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
            when(jwtTokenProvider.getTokenType(refreshToken)).thenReturn("refresh");
            when(jwtTokenProvider.getExpirationDateFromToken(refreshToken)).thenReturn(new Date(System.currentTimeMillis() + 604800000));

            // Act
            authService.logout(accessToken, refreshToken);

            // Assert
            verify(jwtTokenProvider).validateToken(accessToken);
            verify(jwtTokenProvider).validateToken(refreshToken);
            verify(tokenBlacklistService, times(2)).addToBlacklist(anyString(), anyLong());
        }

        @Test
        @DisplayName("登出时令牌无效")
        void testLogout_InvalidTokens() {
            // Arrange
            String invalidToken = "invalid-token";

            when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

            // Act
            authService.logout(invalidToken, invalidToken);

            // Assert
            verify(jwtTokenProvider, times(2)).validateToken(invalidToken);
            verify(tokenBlacklistService, never()).addToBlacklist(anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("令牌验证测试")
    class ValidateTokenTests {

        @Test
        @DisplayName("成功验证有效令牌")
        void testValidateToken_Success() {
            // Arrange
            String token = "valid-token";
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
            when(jwtTokenProvider.getTokenType(token)).thenReturn("access");

            // Act
            boolean isValid = authService.validateToken(token);

            // Assert
            assertTrue(isValid);
            verify(jwtTokenProvider).validateToken(token);
            verify(tokenBlacklistService).isBlacklisted(token);
            verify(jwtTokenProvider).getTokenType(token);
        }

        @Test
        @DisplayName("验证空令牌")
        void testValidateToken_NullToken() {
            // Act
            boolean isValid = authService.validateToken(null);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("验证JWT无效的令牌")
        void testValidateToken_JwtInvalid() {
            // Arrange
            String token = "invalid-token";
            when(jwtTokenProvider.validateToken(token)).thenReturn(false);

            // Act
            boolean isValid = authService.validateToken(token);

            // Assert
            assertFalse(isValid);
            verify(jwtTokenProvider).validateToken(token);
            verify(tokenBlacklistService, never()).isBlacklisted(anyString());
        }

        @Test
        @DisplayName("验证在黑名单中的令牌")
        void testValidateToken_TokenBlacklisted() {
            // Arrange
            String token = "blacklisted-token";
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

            // Act
            boolean isValid = authService.validateToken(token);

            // Assert
            assertFalse(isValid);
            verify(jwtTokenProvider).validateToken(token);
            verify(tokenBlacklistService).isBlacklisted(token);
        }

        @Test
        @DisplayName("验证类型错误的令牌")
        void testValidateToken_WrongTokenType() {
            // Arrange
            String token = "refresh-token";
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
            when(jwtTokenProvider.getTokenType(token)).thenReturn("refresh");

            // Act
            boolean isValid = authService.validateToken(token);

            // Assert
            assertFalse(isValid);
            verify(jwtTokenProvider).validateToken(token);
            verify(tokenBlacklistService).isBlacklisted(token);
            verify(jwtTokenProvider).getTokenType(token);
        }
    }
}