package com.icecream.backend.dto.response;

import lombok.Data;

/**
 * 认证响应
 * 包含JWT令牌和用户基本信息
 */
@Data
public class AuthResponse {
    /**
     * 访问令牌 (Access Token)
     */
    private String accessToken;

    /**
     * 刷新令牌 (Refresh Token)
     */
    private String refreshToken;

    /**
     * 令牌类型，固定为 "Bearer"
     */
    private String tokenType = "Bearer";

    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn,
                        Long userId, String username, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}