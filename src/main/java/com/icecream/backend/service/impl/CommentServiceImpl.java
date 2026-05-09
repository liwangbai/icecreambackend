package com.icecream.backend.service.impl;

import com.github.pagehelper.PageHelper;
import com.icecream.backend.dto.request.CommentCreateRequest;
import com.icecream.backend.dto.request.CommentUpdateRequest;
import com.icecream.backend.exception.ForbiddenException;
import com.icecream.backend.exception.ResourceNotFoundException;
import com.icecream.backend.mapper.CommentMapper;
import com.icecream.backend.mapper.PostMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Comment;
import com.icecream.backend.model.Post;
import com.icecream.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public Comment createComment(Long userId, Long postId, CommentCreateRequest request) {
        log.info("创建评论: userId={}, postId={}", userId, postId);

        // 检查帖子是否存在
        Optional<Post> postOpt = postMapper.findById(postId);
        if (!postOpt.isPresent()) {
            throw new ResourceNotFoundException("帖子不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setStatus(1);

        // 如果是回复，设置rootId和replyToUserId
        if (request.getParentId() != null) {
            Optional<Comment> parentOpt = commentMapper.findById(request.getParentId());
            if (!parentOpt.isPresent()) {
                throw new ResourceNotFoundException("父评论不存在");
            }
            Comment parent = parentOpt.get();
            // 回复的帖子必须是同一个帖子
            if (!parent.getPostId().equals(postId)) {
                throw new ForbiddenException("不能在不同的帖子下回复");
            }
            // 设置parentId
            comment.setParentId(request.getParentId());
            // 设置replyToUserId（回复目标是被回复评论的作者）
            comment.setReplyToUserId(parent.getUserId());
            // 设置rootId：如果父评论是1级评论，rootId就是父评论id；如果是2级评论，rootId继承父评论的rootId
            if (parent.getRootId() == null) {
                comment.setRootId(parent.getId());
            } else {
                comment.setRootId(parent.getRootId());
            }
            // 增加一级评论的replyCount（通过rootId）
            commentMapper.incrementReplyCount(comment.getRootId());
        }

        // 插入评论
        commentMapper.insert(comment);
        log.info("评论创建成功: commentId={}", comment.getId());

        // 增加帖子的评论数
        postMapper.incrementCommentCount(postId);

        return getCommentById(comment.getId(), userId);
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId, Long currentUserId, int page, int size) {
        log.debug("获取帖子评论列表: postId={}, page={}, size={}", postId, page, size);

        // 设置分页
        PageHelper.startPage(page, size);

        // 查询顶级评论
        List<Comment> comments = commentMapper.findTopLevelByPostId(postId, currentUserId);

        // 批量加载所有顶级评论的前3条二级回复
        if (!comments.isEmpty()) {
            List<Long> rootIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
            Map<Long, List<Comment>> repliesMap = commentMapper.findTop3RepliesByRootIds(rootIds, currentUserId)
                    .stream()
                    .collect(Collectors.groupingBy(Comment::getRootId));
            for (Comment comment : comments) {
                comment.setReplies(repliesMap.getOrDefault(comment.getId(), Collections.emptyList()));
            }
        }

        return comments;
    }

    @Override
    public long countCommentsByPostId(Long postId) {
        return commentMapper.countByPostId(postId);
    }

    @Override
    public Comment getCommentById(Long commentId, Long currentUserId) {
        log.debug("获取评论详情: commentId={}", commentId);

        Optional<Comment> commentOpt = commentMapper.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new ResourceNotFoundException("评论不存在");
        }

        Comment comment = commentOpt.get();
        return enrichComment(comment, currentUserId);
    }

    @Override
    @Transactional
    public Comment updateComment(Long commentId, Long userId, CommentUpdateRequest request) {
        log.info("更新评论: commentId={}, userId={}", commentId, commentId);

        Optional<Comment> commentOpt = commentMapper.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new ResourceNotFoundException("评论不存在");
        }

        Comment comment = commentOpt.get();
        // 检查是否有权限更新
        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException("没有权限更新此评论");
        }

        // 更新评论内容
        comment.setContent(request.getContent());
        commentMapper.update(comment);

        return getCommentById(commentId, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("删除评论: commentId={}, userId={}", commentId, userId);

        Optional<Comment> commentOpt = commentMapper.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new ResourceNotFoundException("评论不存在");
        }

        Comment comment = commentOpt.get();

        // 检查是否有权限删除：评论作者或帖主
        boolean isAuthor = comment.getUserId().equals(userId);
        Optional<Post> postOpt = postMapper.findById(comment.getPostId());
        boolean isPostAuthor = postOpt.isPresent() && postOpt.get().getUserId().equals(userId);

        if (!isAuthor && !isPostAuthor) {
            throw new ForbiddenException("没有权限删除此评论");
        }

        // 如果是二级评论，减少对应一级评论的replyCount
        if (comment.getRootId() != null) {
            commentMapper.decrementReplyCount(comment.getRootId());
        }

        // 减少帖子的评论数
        postMapper.decrementCommentCount(comment.getPostId());

        // 软删除评论（status=0），由于查询时过滤status=1，被软删除的评论不会展示
        commentMapper.softDelete(commentId);

        log.info("评论删除成功: commentId={}", commentId);
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        log.info("点赞评论: commentId={}, userId={}", commentId, userId);

        // 检查评论是否存在
        Optional<Comment> commentOpt = commentMapper.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new ResourceNotFoundException("评论不存在");
        }

        // 检查是否已点赞
        if (commentMapper.existsLike(userId, commentId)) {
            throw new RuntimeException("已经点赞过此评论");
        }

        // 添加点赞记录
        commentMapper.insertLike(userId, commentId);

        // 增加评论点赞数
        commentMapper.incrementLikeCount(commentId);

        // 增加评论作者的获赞数
        Comment comment = commentOpt.get();
        userMapper.incrementLikeCount(comment.getUserId());
        cacheManager.getCache("users").evict(comment.getUserId());
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        log.info("取消点赞评论: commentId={}, userId={}", commentId, userId);

        // 检查评论是否存在
        Optional<Comment> commentOpt = commentMapper.findById(commentId);
        if (!commentOpt.isPresent()) {
            throw new ResourceNotFoundException("评论不存在");
        }

        // 检查是否已点赞
        if (!commentMapper.existsLike(userId, commentId)) {
            throw new RuntimeException("尚未点赞此评论");
        }

        // 删除点赞记录
        commentMapper.deleteLike(userId, commentId);

        // 减少评论点赞数
        commentMapper.decrementLikeCount(commentId);

        // 减少评论作者的获赞数
        Comment comment = commentOpt.get();
        userMapper.decrementLikeCount(comment.getUserId());
        cacheManager.getCache("users").evict(comment.getUserId());
    }

    @Override
    public boolean isLiked(Long commentId, Long userId) {
        return commentMapper.existsLike(userId, commentId);
    }

    @Override
    public List<Comment> getRepliesByRootId(Long rootId, Long currentUserId, int page, int size) {
        log.debug("获取一级评论下的二级回复列表: rootId={}, page={}, size={}", rootId, page, size);

        // 设置分页
        PageHelper.startPage(page, size);

        return commentMapper.findRepliesByRootId(rootId, currentUserId);
    }

    @Override
    public long countRepliesByRootId(Long rootId) {
        return commentMapper.countByRootId(rootId);
    }

    // ========== 私有方法 ==========

    /**
     * 丰富评论信息（设置作者、是否点赞等）
     */
    private Comment enrichComment(Comment comment, Long currentUserId) {
        // 设置是否点赞
        if (currentUserId != null) {
            comment.setLiked(commentMapper.existsLike(currentUserId, comment.getId()));
        } else {
            comment.setLiked(false);
        }
        return comment;
    }
}