package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.request.UserUpdateRequest;
import com.icecream.backend.model.User;
import com.icecream.backend.service.UserService;
import com.icecream.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 * 处理用户信息管理和关注关系相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理和关注关系相关接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的完整信息")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("获取当前用户信息: userId={}", currentUserId);
        User user = userService.getCurrentUser(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", user));
    }

    @PutMapping("/me")
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的个人信息")
    public ResponseEntity<ApiResponse<User>> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("更新用户信息: userId={}", currentUserId);
        User updatedUser = userService.updateUser(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户公开信息", description = "根据用户ID获取用户的公开信息")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long userId) {
        log.info("获取用户信息: userId={}", userId);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", user));
    }

    @PostMapping("/{userId}/follow")
    @Operation(summary = "关注用户", description = "关注指定用户")
    public ResponseEntity<ApiResponse<Void>> followUser(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("关注用户: followerId={}, followingId={}", currentUserId, userId);
        userService.followUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("关注成功"));
    }

    @DeleteMapping("/{userId}/follow")
    @Operation(summary = "取消关注", description = "取消关注指定用户")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("取消关注: followerId={}, followingId={}", currentUserId, userId);
        userService.unfollowUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("取消关注成功"));
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "获取粉丝列表", description = "获取指定用户的粉丝列表")
    public ResponseEntity<ApiResponse<List<User>>> getFollowers(@PathVariable Long userId) {
        log.info("获取粉丝列表: userId={}", userId);
        List<User> followers = userService.getFollowers(userId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", followers));
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "获取关注列表", description = "获取指定用户的关注列表")
    public ResponseEntity<ApiResponse<List<User>>> getFollowing(@PathVariable Long userId) {
        log.info("获取关注列表: userId={}", userId);
        List<User> following = userService.getFollowing(userId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", following));
    }

    @GetMapping("/{userId}/is-following")
    @Operation(summary = "检查是否关注", description = "检查当前用户是否关注了指定用户")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("检查关注关系: followerId={}, followingId={}", currentUserId, userId);
        boolean isFollowing = userService.isFollowing(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("检查成功", isFollowing));
    }

}