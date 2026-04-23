package com.icecream.backend.mapper;

import com.icecream.backend.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 评论数据访问接口
 */
@Mapper
public interface CommentMapper {

    /**
     * 根据ID查询评论
     */
    Optional<Comment> findById(@Param("id") Long id);

    /**
     * 查询帖子的顶级评论列表（parentId为null）
     */
    List<Comment> findTopLevelByPostId(@Param("postId") Long postId, @Param("currentUserId") Long currentUserId);

    /**
     * 统计帖子的顶级评论数量
     */
    long countByPostId(@Param("postId") Long postId);

    /**
     * 查询某一级评论的子回复列表
     */
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId, @Param("currentUserId") Long currentUserId);

    /**
     * 统计某一级评论的子回复数量
     */
    long countByParentId(@Param("parentId") Long parentId);

    /**
     * 插入评论
     */
    int insert(Comment comment);

    /**
     * 更新评论内容
     */
    int update(Comment comment);

    /**
     * 软删除评论（status设为0）
     */
    int softDelete(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    int incrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 减少点赞数
     */
    int decrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 增加回复数
     */
    int incrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 减少回复数
     */
    int decrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 检查用户是否点赞了评论
     */
    boolean existsLike(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /**
     * 添加评论点赞记录
     */
    int insertLike(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /**
     * 删除评论点赞记录
     */
    int deleteLike(@Param("userId") Long userId, @Param("commentId") Long commentId);
}