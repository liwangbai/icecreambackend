package com.icecream.backend.service;

import com.icecream.backend.dto.request.LoginRequest;
import com.icecream.backend.dto.request.RefreshTokenRequest;
import com.icecream.backend.dto.request.RegisterRequest;
import com.icecream.backend.dto.response.AuthResponse;

/**
 * 认证服务接口
 * 负责用户认证、注册、令牌管理等功能
 */
public interface AuthService {

    /**
     * 用户注册
     * @param request 注册请求
     * @return 认证响应（包含JWT令牌）
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 用户登录
     * @param request 登录请求
     * @return 认证响应（包含JWT令牌）
     */
    AuthResponse login(LoginRequest request);

    /**
     * 刷新访问令牌
     * @param request 刷新令牌请求
     * @return 新的认证响应
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * 用户登出
     * 将令牌加入黑名单，使其失效
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌（可选）
     */
    void logout(String accessToken, String refreshToken);

    /**
     * 验证令牌是否有效且不在黑名单中
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
}