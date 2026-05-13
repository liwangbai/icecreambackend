package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.PagedResult;
import com.icecream.backend.dto.request.CommentCreateRequest;
import com.icecream.backend.dto.request.CommentUpdateRequest;
import com.icecream.backend.model.Comment;
import com.icecream.backend.service.CommentService;
import com.icecream.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "评论相关操作接口，包括创建、查询、更新、删除、点赞等")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "创建评论", description = "在指定帖子下创建评论，支持楼中楼回复")
    public ResponseEntity<ApiResponse<Comment>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("创建评论: userId={}, postId={}", currentUserId, postId);
        Comment comment = commentService.createComment(currentUserId, postId, request);
        return ResponseEntity.ok(ApiResponse.success("评论创建成功", comment));
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "获取帖子评论列表", description = "获取指定帖子的评论列表，每个一级评论包含前3条回复")
    public ResponseEntity<ApiResponse<PagedResult<Comment>>> getCommentsByPostId(
            @PathVariable Long postId,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        log.debug("获取帖子评论列表: postId={}, page={}, size={}", postId, page, size);
        List<Comment> comments = commentService.getCommentsByPostId(postId, currentUserId, page, size);
        long total = commentService.countCommentsByPostId(postId);
        PagedResult<Comment> pagedResult = PagedResult.of(comments, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/comments/{commentId}")
    @Operation(summary = "获取评论详情", description = "根据ID获取评论详情")
    public ResponseEntity<ApiResponse<Comment>> getCommentById(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        log.debug("获取评论详情: commentId={}", commentId);
        Comment comment = commentService.getCommentById(commentId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", comment));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "更新评论", description = "更新评论内容，仅评论作者可以更新")
    public ResponseEntity<ApiResponse<Comment>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("更新评论: commentId={}, userId={}", commentId, currentUserId);
        Comment comment = commentService.updateComment(commentId, currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("评论更新成功", comment));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除评论", description = "删除评论，评论作者或帖主均可删除")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("删除评论: commentId={}, userId={}", commentId, currentUserId);
        commentService.deleteComment(commentId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("评论删除成功"));
    }

    @PostMapping("/comments/{commentId}/like")
    @Operation(summary = "点赞评论", description = "给指定评论点赞")
    public ResponseEntity<ApiResponse<Void>> likeComment(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("点赞评论: commentId={}, userId={}", commentId, currentUserId);
        commentService.likeComment(commentId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("点赞成功"));
    }

    @DeleteMapping("/comments/{commentId}/like")
    @Operation(summary = "取消点赞评论", description = "取消对指定评论的点赞")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("取消点赞评论: commentId={}, userId={}", commentId, currentUserId);
        commentService.unlikeComment(commentId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功"));
    }

    @GetMapping("/comments/{commentId}/is-liked")
    @Operation(summary = "检查是否点赞评论", description = "检查当前用户是否点赞了指定评论")
    public ResponseEntity<ApiResponse<Boolean>> isCommentLiked(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("检查是否点赞评论: commentId={}, userId={}", commentId, currentUserId);
        boolean isLiked = commentService.isLiked(commentId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("检查成功", isLiked));
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "获取一级评论的二级回复列表", description = "获取一级评论下的所有二级回复（分页，通过rootId查询）")
    public ResponseEntity<ApiResponse<PagedResult<Comment>>> getRepliesByRootId(
            @PathVariable Long commentId,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "10") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        log.debug("获取二级回复列表: rootId={}, page={}, size={}", commentId, page, size);
        List<Comment> replies = commentService.getRepliesByRootId(commentId, currentUserId, page, size);
        long total = commentService.countRepliesByRootId(commentId);
        PagedResult<Comment> pagedResult = PagedResult.of(replies, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }
}