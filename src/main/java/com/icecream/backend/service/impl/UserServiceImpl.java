package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.UserUpdateRequest;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.User;
import com.icecream.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getCurrentUser(Long userId) {
        log.debug("获取当前用户信息: userId={}", userId);
        Optional<User> userOpt = userMapper.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        return userOpt.get();
    }

    @Override
    public User getUserById(Long userId) {
        log.debug("获取用户公开信息: userId={}", userId);
        Optional<User> userOpt = userMapper.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        User user = userOpt.get();
        // 移除敏感信息（如密码哈希）
        user.setPasswordHash(null);
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        log.info("更新用户信息: userId={}", userId);

        Optional<User> userOpt = userMapper.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        User user = userOpt.get();

        // 更新允许修改的字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // 检查邮箱是否已被其他用户使用
            Optional<User> emailUserOpt = userMapper.findByEmail(request.getEmail());
            if (emailUserOpt.isPresent() && !emailUserOpt.get().getId().equals(userId)) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
        }

        // 更新数据库
        userMapper.update(user);

        return getUserById(userId);
    }

    @Override
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        log.info("关注用户: followerId={}, followingId={}", followerId, followingId);

        // 检查不能关注自己
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }

        // 检查用户是否存在
        Optional<User> followerOpt = userMapper.findById(followerId);
        Optional<User> followingOpt = userMapper.findById(followingId);
        if (!followerOpt.isPresent() || !followingOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否已关注
        if (userMapper.existsFollow(followerId, followingId)) {
            throw new RuntimeException("已经关注了该用户");
        }

        // 添加关注关系
        userMapper.insertFollow(followerId, followingId);

        // 更新关注者和被关注者的统计信息
        userMapper.incrementFollowingCount(followerId);  // 关注者的关注数+1
        userMapper.incrementFollowerCount(followingId);  // 被关注者的粉丝数+1

        log.info("关注成功: followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        log.info("取消关注: followerId={}, followingId={}", followerId, followingId);

        // 检查关注关系是否存在
        if (!userMapper.existsFollow(followerId, followingId)) {
            throw new RuntimeException("尚未关注该用户");
        }

        // 删除关注关系
        userMapper.deleteFollow(followerId, followingId);

        // 更新关注者和被关注者的统计信息
        userMapper.decrementFollowingCount(followerId);  // 关注者的关注数-1
        userMapper.decrementFollowerCount(followingId);  // 被关注者的粉丝数-1

        log.info("取消关注成功: followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return userMapper.existsFollow(followerId, followingId);
    }

    @Override
    public List<User> getFollowers(Long userId) {
        log.debug("获取粉丝列表: userId={}", userId);

        List<User> followers = userMapper.findFollowers(userId);

        // 移除敏感信息
        for (User user : followers) {
            user.setPasswordHash(null);
        }

        return followers;
    }

    @Override
    public List<User> getFollowing(Long userId) {
        log.debug("获取关注列表: userId={}", userId);

        List<User> following = userMapper.findFollowing(userId);

        // 移除敏感信息
        for (User user : following) {
            user.setPasswordHash(null);
        }

        return following;
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        log.debug("更新最后登录时间: userId={}", userId);
        userMapper.updateLastLogin(userId);
    }
}