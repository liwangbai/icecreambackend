package com.icecream.backend.service;

import com.icecream.backend.dto.request.ChangePasswordRequest;
import com.icecream.backend.dto.request.PrivacySettingsRequest;
import com.icecream.backend.dto.request.UserUpdateRequest;
import com.icecream.backend.dto.response.PrivacySettingsResponse;
import com.icecream.backend.dto.response.UserInfoResponse;
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
     * 根据ID获取用户公开信息（含关注状态）
     * @param userId 用户ID
     * @param currentUserId 当前用户ID，可为null（游客模式）
     * @return 用户信息（含关注状态）
     */
    UserInfoResponse getUserProfile(Long userId, Long currentUserId);

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
     * 获取用户的粉丝列表（含互关状态，支持分页）
     * @param userId 用户ID
     * @param currentUserId 当前登录用户ID
     * @param page 页码（PageHelper页码，从1开始）
     * @param size 每页大小
     * @return 粉丝用户列表
     */
    List<UserInfoResponse> getFollowers(Long userId, Long currentUserId, int page, int size);

    /**
     * 获取用户的关注列表（含互关状态，支持分页）
     * @param userId 用户ID
     * @param currentUserId 当前登录用户ID
     * @param page 页码（PageHelper页码，从1开始）
     * @param size 每页大小
     * @return 关注用户列表
     */
    List<UserInfoResponse> getFollowing(Long userId, Long currentUserId, int page, int size);

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

    /**
     * 根据昵称关键词搜索用户
     * @param keyword 搜索关键词
     * @param currentUserId 当前用户ID，可为null（游客模式）
     * @return 用户信息列表
     */
    List<UserInfoResponse> searchByNickname(String keyword, Long currentUserId);

    /**
     * 获取当前用户的隐私设置
     * @param userId 用户ID
     * @return 隐私设置
     */
    PrivacySettingsResponse getPrivacySettings(Long userId);

    /**
     * 更新当前用户的隐私设置
     * @param userId 用户ID
     * @param request 隐私设置请求
     * @return 更新后的隐私设置
     */
    PrivacySettingsResponse updatePrivacySettings(Long userId, PrivacySettingsRequest request);

    /**
     * 注销账号（软删除，设置status=0）
     * @param userId 用户ID
     */
    void deleteAccount(Long userId);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param request 修改密码请求（旧密码、新密码）
     */
    void changePassword(Long userId, ChangePasswordRequest request);
}