package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * 对应数据库中的users表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    /**
     * 用户名，唯一
     */
    private String username;

    /**
     * 邮箱地址，唯一
     */
    private String email;

    /**
     * 密码哈希值（BCrypt加密）
     */
    private String passwordHash;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 账户状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 用户角色：ROLE_USER, ROLE_ADMIN等
     */
    private String role;

    /**
     * 最后登录时间
     */
    private java.time.LocalDateTime lastLoginAt;

    /**
     * 发帖数
     */
    private Integer postCount;

    /**
     * 粉丝数
     */
    private Integer followerCount;

    /**
     * 关注数
     */
    private Integer followingCount;

    /**
     * 获赞数（收到的帖子点赞数 + 收到的评论点赞数）
     */
    private Integer likeCount;

    /**
     * 收藏的帖子数
     */
    private Integer collectionCount;

    /**
     * 浏览历史条数
     */
    private Integer historyCount;
}