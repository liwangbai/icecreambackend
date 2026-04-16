package com.icecream.backend.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求
 */
@Data
public class LoginRequest {
    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名或邮箱不能为空")
    private String usernameOrEmail;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}