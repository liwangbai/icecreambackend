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
     * 查询某评论的直接子回复列表
     */
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId, @Param("currentUserId") Long currentUserId);

    /**
     * 查询某一级评论下的所有二级评论（最多3条，用于评论列表初始加载）
     */
    List<Comment> findTop3RepliesByRootId(@Param("rootId") Long rootId, @Param("currentUserId") Long currentUserId);

    /**
     * 批量查询多个一级评论下的前3条二级回复
     */
    List<Comment> findTop3RepliesByRootIds(@Param("rootIds") List<Long> rootIds, @Param("currentUserId") Long currentUserId);

    /**
     * 查询某一级评论下的所有二级评论（分页，用于查看更多回复）
     */
    List<Comment> findRepliesByRootId(@Param("rootId") Long rootId, @Param("currentUserId") Long currentUserId);

    /**
     * 统计某一级评论的二级回复数量
     */
    long countByRootId(@Param("rootId") Long rootId);

    /**
     * 统计某评论的直接子回复数量
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
     * 增加一级评论的回复数（通过rootId）
     */
    int incrementReplyCount(@Param("rootId") Long rootId);

    /**
     * 减少一级评论的回复数（通过rootId）
     */
    int decrementReplyCount(@Param("rootId") Long rootId);

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

    /**
     * 查询评论和@通知列表（评论我的帖子 + @我的回复，UNION合并按时间倒序）
     * @param userId 当前用户ID
     * @return 评论通知列表
     */
    List<com.icecream.backend.dto.response.CommentNotification> findCommentsAndMentions(@Param("userId") Long userId);

    /**
     * 统计评论和@通知数量
     * @param userId 当前用户ID
     * @return 通知数量
     */
    long countCommentsAndMentions(@Param("userId") Long userId);
}
