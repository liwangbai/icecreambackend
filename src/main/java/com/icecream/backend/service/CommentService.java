package com.icecream.backend.service;

import com.icecream.backend.dto.request.CommentCreateRequest;
import com.icecream.backend.dto.request.CommentUpdateRequest;
import com.icecream.backend.model.Comment;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 创建评论
     */
    Comment createComment(Long userId, Long postId, CommentCreateRequest request);

    /**
     * 获取帖子的评论列表（分页，包含前3条回复）
     */
    List<Comment> getCommentsByPostId(Long postId, Long currentUserId, int page, int size);

    /**
     * 统计帖子的顶级评论数量
     */
    long countCommentsByPostId(Long postId);

    /**
     * 获取评论详情
     */
    Comment getCommentById(Long commentId, Long currentUserId);

    /**
     * 更新评论
     * 仅评论作者可更新
     */
    Comment updateComment(Long commentId, Long userId, CommentUpdateRequest request);

    /**
     * 删除评论
     * 评论作者或帖主可删除
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 点赞评论
     */
    void likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     */
    void unlikeComment(Long commentId, Long userId);

    /**
     * 检查用户是否点赞了评论
     */
    boolean isLiked(Long commentId, Long userId);

    /**
     * 获取一级评论的所有回复（分页）
     */
    List<Comment> getRepliesByParentId(Long parentId, Long currentUserId, int page, int size);

    /**
     * 统计一级评论的回复数量
     */
    long countRepliesByParentId(Long parentId);
}