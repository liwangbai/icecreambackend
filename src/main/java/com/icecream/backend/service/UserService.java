package com.icecream.backend.service;

import com.icecream.backend.dto.request.UserUpdateRequest;
import com.icecream.backend.model.User;

import java.util.List;

/**
 * 用户服务接口
 * 负责用户信息管理、关注关系等功能
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getCurrentUser(Long userId);

    /**
     * 根据ID获取用户信息（公开信息）
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    User updateUser(Long userId, UserUpdateRequest request);

    /**
     * 关注用户
     * @param followerId 关注者ID（当前用户）
     * @param followingId 被关注者ID
     */
    void followUser(Long followerId, Long followingId);

    /**
     * 取消关注
     * @param followerId 关注者ID（当前用户）
     * @param followingId 被关注者ID
     */
    void unfollowUser(Long followerId, Long followingId);

    /**
     * 检查是否关注了某个用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取用户的粉丝列表
     * @param userId 用户ID
     * @return 粉丝用户列表
     */
    List<User> getFollowers(Long userId);

    /**
     * 获取用户的关注列表
     * @param userId 用户ID
     * @return 关注用户列表
     */
    List<User> getFollowing(Long userId);

    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     */
    void updateLastLogin(Long userId);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String updateAvatar(Long userId, org.springframework.web.multipart.MultipartFile file);
}