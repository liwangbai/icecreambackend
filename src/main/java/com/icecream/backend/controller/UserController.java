package com.icecream.backend.controller;

import com.github.pagehelper.Page;
import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.PagedResult;
import com.icecream.backend.dto.request.PrivacySettingsRequest;
import com.icecream.backend.dto.request.UserUpdateRequest;
import com.icecream.backend.dto.response.PrivacySettingsResponse;
import com.icecream.backend.dto.response.UserInfoResponse;
import com.icecream.backend.model.User;
import com.icecream.backend.service.UserService;
import com.icecream.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(summary = "获取用户公开信息", description = "根据用户ID获取用户的公开信息，包含关注状态（isFollowing、isFollowed）")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserById(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        log.info("获取用户信息: userId={}, currentUserId={}", userId, currentUserId);
        UserInfoResponse userInfo = userService.getUserProfile(userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", userInfo));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据昵称关键词搜索用户，支持分页")
    public ResponseEntity<ApiResponse<PagedResult<UserInfoResponse>>> searchUsers(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        log.info("搜索用户: keyword={}, currentUserId={}, page={}, size={}", keyword, currentUserId, page, size);
        com.github.pagehelper.PageHelper.startPage(page + 1, size);
        List<UserInfoResponse> users = userService.searchByNickname(keyword, currentUserId);
        long total = ((Page) users).getTotal();
        PagedResult<UserInfoResponse> pagedResult = PagedResult.of(users, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("搜索成功", pagedResult));
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
    @Operation(summary = "获取粉丝列表", description = "获取指定用户的粉丝列表，支持分页，包含互关状态")
    public ResponseEntity<ApiResponse<PagedResult<UserInfoResponse>>> getFollowers(
            @PathVariable Long userId,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("获取粉丝列表: userId={}, currentUserId={}, page={}, size={}", userId, currentUserId, page, size);
        List<UserInfoResponse> followers = userService.getFollowers(userId, currentUserId, page + 1, size);
        long total = ((Page) followers).getTotal();
        PagedResult<UserInfoResponse> pagedResult = PagedResult.of(followers, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "获取关注列表", description = "获取指定用户的关注列表，支持分页，包含互关状态")
    public ResponseEntity<ApiResponse<PagedResult<UserInfoResponse>>> getFollowing(
            @PathVariable Long userId,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("获取关注列表: userId={}, currentUserId={}, page={}, size={}", userId, currentUserId, page, size);
        List<UserInfoResponse> following = userService.getFollowing(userId, currentUserId, page + 1, size);
        long total = ((Page) following).getTotal();
        PagedResult<UserInfoResponse> pagedResult = PagedResult.of(following, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/{userId}/is-following")
    @Operation(summary = "检查是否关注", description = "检查当前用户是否关注了指定用户")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("检查关注关系: followerId={}, followingId={}", currentUserId, userId);
        boolean isFollowing = userService.isFollowing(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("检查成功", isFollowing));
    }

    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改用户头像", description = "上传并更新当前用户的头像")
    public ResponseEntity<ApiResponse<String>> updateAvatar(
            @Parameter(description = "头像文件", required = true)
            @RequestParam("file") MultipartFile file) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("更新用户头像: userId={}", currentUserId);
        String avatarUrl = userService.updateAvatar(currentUserId, file);
        return ResponseEntity.ok(ApiResponse.success("头像更新成功", avatarUrl));
    }

    @GetMapping("/me/settings")
    @Operation(summary = "获取隐私设置", description = "获取当前用户的隐私设置（关注列表/粉丝列表可见性）")
    public ResponseEntity<ApiResponse<PrivacySettingsResponse>> getPrivacySettings() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("获取隐私设置: userId={}", currentUserId);
        PrivacySettingsResponse settings = userService.getPrivacySettings(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", settings));
    }

    @PutMapping("/me/settings")
    @Operation(summary = "更新隐私设置", description = "更新当前用户的隐私设置（关注列表/粉丝列表可见性）")
    public ResponseEntity<ApiResponse<PrivacySettingsResponse>> updatePrivacySettings(
            @Valid @RequestBody PrivacySettingsRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("更新隐私设置: userId={}, followingVisibility={}, followerVisibility={}",
                currentUserId, request.getFollowingVisibility(), request.getFollowerVisibility());
        PrivacySettingsResponse settings = userService.updatePrivacySettings(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", settings));
    }

}