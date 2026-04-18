package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.LoginRequest;
import com.icecream.backend.dto.request.RefreshTokenRequest;
import com.icecream.backend.dto.request.RegisterRequest;
import com.icecream.backend.dto.response.AuthResponse;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.User;
import com.icecream.backend.security.JwtTokenProvider;
import com.icecream.backend.security.TokenBlacklistService;
import com.icecream.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icecream.backend.exception.ConflictException;
import com.icecream.backend.exception.UnauthorizedException;
import java.util.Date;
import java.util.Optional;

/**
 * 认证服务实现类
 * TODO: 需要集成JWT令牌生成和验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("用户注册: phone={}", request.getPhone());

        // 检查用户名是否已存在（手机号作为账号存入username）
        Optional<User> existingUserByUsername = userMapper.findByUsername(request.getPhone());
        if (existingUserByUsername.isPresent()) {
            throw new ConflictException("该手机号已注册");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname("番薯" + request.getPhone().substring(request.getPhone().length() - 4));
        user.setAvatarUrl(null);
        user.setBio(null);
        user.setGender(0); // 默认未知
        user.setStatus(1); // 启用
        user.setRole("ROLE_USER");
        user.setLastLoginAt(null);
        user.setPostCount(0);
        user.setFollowerCount(0);
        user.setFollowingCount(0);

        // 插入数据库
        userMapper.insert(user);

        log.info("用户注册成功: userId={}, phone={}", user.getId(), request.getPhone());

        // 生成JWT令牌
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 构建响应
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000); // 转换为秒

        log.debug("用户注册令牌生成成功: userId={}, accessTokenLength={}, refreshTokenLength={}",
                user.getId(), accessToken.length(), refreshToken.length());

        return response;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("用户登录: phone={}", request.getPhone());

        // 根据手机号查找用户
        Optional<User> userOpt = userMapper.findByUsername(request.getPhone());
        if (!userOpt.isPresent()) {
            throw new UnauthorizedException("手机号或密码错误");
        }

        User user = userOpt.get();

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("手机号或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 更新最后登录时间
        userMapper.updateLastLogin(user.getId());

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 生成JWT令牌
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 构建响应
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000); // 转换为秒

        log.debug("用户登录令牌生成成功: userId={}, accessTokenLength={}, refreshTokenLength={}",
                user.getId(), accessToken.length(), refreshToken.length());

        return response;
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("刷新令牌请求");

        String refreshToken = request.getRefreshToken();

        // 验证刷新令牌
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("刷新令牌无效");
            throw new RuntimeException("刷新令牌无效");
        }

        // 检查令牌类型
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            log.warn("令牌类型错误，期望refresh令牌，实际为: {}", tokenType);
            throw new RuntimeException("令牌类型错误");
        }

        // 检查刷新令牌是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            log.warn("刷新令牌已被加入黑名单");
            throw new RuntimeException("刷新令牌已失效");
        }

        // 从刷新令牌中提取用户名
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 根据用户名查找用户
        Optional<User> userOpt = userMapper.findByUsername(username);
        if (!userOpt.isPresent()) {
            log.warn("用户不存在: username={}", username);
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: username={}", username);
            throw new RuntimeException("用户已被禁用");
        }

        // 将旧的刷新令牌加入黑名单（防止重复使用）
        tokenBlacklistService.addToBlacklist(refreshToken, 7 * 24 * 60 * 60L); // 7天

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 构建响应
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000); // 转换为秒

        log.info("令牌刷新成功: userId={}, username={}", user.getId(), user.getUsername());

        return response;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        log.info("用户登出");

        // 验证访问令牌
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            // 获取访问令牌的过期时间
            String username = jwtTokenProvider.getUsernameFromToken(accessToken);
            String tokenType = jwtTokenProvider.getTokenType(accessToken);

            if ("access".equals(tokenType)) {
                // 计算访问令牌的剩余过期时间（秒）
                long expiresInSeconds = calculateTokenRemainingTime(accessToken);
                if (expiresInSeconds > 0) {
                    // 将访问令牌加入黑名单
                    tokenBlacklistService.addToBlacklist(accessToken, expiresInSeconds);
                    log.info("访问令牌已加入黑名单: username={}, expiresIn={}秒", username, expiresInSeconds);
                }
            }
        }

        // 验证刷新令牌
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            // 获取刷新令牌的过期时间
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            String tokenType = jwtTokenProvider.getTokenType(refreshToken);

            if ("refresh".equals(tokenType)) {
                // 计算刷新令牌的剩余过期时间（秒）
                long expiresInSeconds = calculateTokenRemainingTime(refreshToken);
                if (expiresInSeconds > 0) {
                    // 将刷新令牌加入黑名单
                    tokenBlacklistService.addToBlacklist(refreshToken, expiresInSeconds);
                    log.info("刷新令牌已加入黑名单: username={}, expiresIn={}秒", username, expiresInSeconds);
                }
            }
        }

        log.info("用户登出完成");
    }

    /**
     * 计算令牌的剩余过期时间（秒）
     *
     * @param token JWT令牌
     * @return 剩余过期时间（秒），如果令牌无效则返回0
     */
    private long calculateTokenRemainingTime(String token) {
        try {
            Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
            long remainingMillis = expirationDate.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMillis / 1000); // 转换为秒
        } catch (Exception e) {
            log.warn("计算令牌剩余时间失败: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("验证令牌: tokenLength={}", token != null ? token.length() : 0);

        // 检查令牌是否为空
        if (token == null || token.isEmpty()) {
            log.debug("令牌为空");
            return false;
        }

        // 验证JWT令牌是否有效
        if (!jwtTokenProvider.validateToken(token)) {
            log.debug("JWT令牌无效");
            return false;
        }

        // 检查令牌是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.debug("令牌在黑名单中");
            return false;
        }

        // 检查令牌类型
        String tokenType = jwtTokenProvider.getTokenType(token);
        if (!"access".equals(tokenType)) {
            log.debug("令牌类型错误，期望access令牌，实际为: {}", tokenType);
            return false;
        }

        log.debug("令牌验证成功");
        return true;
    }
}