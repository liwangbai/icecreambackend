package com.icecream.backend.dto.request;

import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateRequest {
    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 个人简介
     */
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 邮箱（需要验证唯一性）
     */
    private String email;
}