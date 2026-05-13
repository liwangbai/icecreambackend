package com.icecream.backend.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌请求
 */
@Data
public class RefreshTokenRequest {
    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}