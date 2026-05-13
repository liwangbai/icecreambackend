package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.PagedResult;
import com.icecream.backend.dto.response.CommentNotification;
import com.icecream.backend.dto.response.FollowNotification;
import com.icecream.backend.dto.response.InteractionNotification;
import com.icecream.backend.service.NotificationService;
import com.icecream.backend.util.SecurityUtil;
import com.github.pagehelper.PageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 * 处理用户互动通知、评论通知、关注通知相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "用户互动通知、评论通知、关注通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/interactions")
    @Operation(summary = "获取赞和收藏列表", description = "获取对当前用户内容的赞和收藏互动列表（帖子点赞、评论点赞、帖子收藏），按时间倒序排列")
    public ResponseEntity<ApiResponse<PagedResult<InteractionNotification>>> getInteractions(
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取互动通知: userId={}, page={}, size={}", currentUserId, page, size);

        PageHelper.startPage(page + 1, size);
        List<InteractionNotification> interactions = notificationService.getInteractions(currentUserId);
        long total = notificationService.countInteractions(currentUserId);

        PagedResult<InteractionNotification> pagedResult = PagedResult.of(interactions, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/followers")
    @Operation(summary = "获取新增关注列表", description = "获取当前用户的新增粉丝列表，包含是否回关标志，按关注时间倒序排列")
    public ResponseEntity<ApiResponse<PagedResult<FollowNotification>>> getFollowers(
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取关注通知: userId={}, page={}, size={}", currentUserId, page, size);

        PageHelper.startPage(page + 1, size);
        List<FollowNotification> followers = notificationService.getFollowers(currentUserId);
        long total = notificationService.countFollowers(currentUserId);

        PagedResult<FollowNotification> pagedResult = PagedResult.of(followers, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/comments")
    @Operation(summary = "获取评论和@列表", description = "获取评论了当前用户帖子以及@了当前用户的评论列表，按时间倒序排列")
    public ResponseEntity<ApiResponse<PagedResult<CommentNotification>>> getComments(
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取评论通知: userId={}, page={}, size={}", currentUserId, page, size);

        PageHelper.startPage(page + 1, size);
        List<CommentNotification> comments = notificationService.getCommentsAndMentions(currentUserId);
        long total = notificationService.countCommentsAndMentions(currentUserId);

        PagedResult<CommentNotification> pagedResult = PagedResult.of(comments, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }
}
