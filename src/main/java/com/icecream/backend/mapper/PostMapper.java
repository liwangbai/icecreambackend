package com.icecream.backend.mapper;

import com.icecream.backend.dto.request.PostQueryRequest;
import com.icecream.backend.model.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 帖子数据访问接口
 * 对应数据库中的posts表
 */
@Mapper
public interface PostMapper {

    // ========== 基本CRUD操作 ==========

    /**
     * 根据ID查询帖子
     * @param id 帖子ID
     * @return 帖子信息（Optional）
     */
    Optional<Post> findById(@Param("id") Long id);

    /**
     * 查询所有帖子（仅管理员使用）
     * @return 帖子列表
     */
    List<Post> findAll();

    /**
     * 插入新帖子
     * @param post 帖子对象
     * @return 影响的行数
     */
    int insert(Post post);

    /**
     * 更新帖子信息
     * @param post 帖子对象
     * @return 影响的行数
     */
    int update(Post post);

    /**
     * 根据ID删除帖子
     * @param id 帖子ID
     * @return 影响的行数
     */
    int delete(@Param("id") Long id);

    // ========== 条件查询（基础查询） ==========

    /**
     * 根据条件查询帖子列表（支持分页）
     * @param query 查询条件
     * @return 帖子列表
     */
    List<Post> findByCondition(@Param("query") PostQueryRequest query);

    /**
     * 根据条件统计帖子数量
     * @param query 查询条件
     * @return 帖子数量
     */
    long countByCondition(@Param("query") PostQueryRequest query);

    // ========== 业务方法 ==========

    /**
     * 增加帖子浏览数
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int incrementViewCount(@Param("postId") Long postId);

    /**
     * 增加帖子点赞数
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int incrementLikeCount(@Param("postId") Long postId);

    /**
     * 增加帖子评论数
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int incrementCommentCount(@Param("postId") Long postId);

    /**
     * 减少帖子点赞数
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int decrementLikeCount(@Param("postId") Long postId);

    /**
     * 减少帖子评论数
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int decrementCommentCount(@Param("postId") Long postId);

    // ========== 用户相关查询 ==========

    /**
     * 根据用户ID查询帖子
     * @param userId 用户ID
     * @param status 帖子状态（可选）
     * @return 帖子列表
     */
    List<Post> findByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户关注的用户的帖子（时间线）
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> findFollowingPosts(@Param("userId") Long userId);

    // ========== 标签相关查询 ==========

    /**
     * 根据标签ID查询帖子
     * @param tagId 标签ID
     * @return 帖子列表
     */
    List<Post> findByTagId(@Param("tagId") Long tagId);

    /**
     * 查询最近有标签的帖子（用于统计热门标签）
     * @param days 天数
     * @return 帖子列表（只包含id和tags字段）
     */
    List<Post> findRecentPostsWithTags(@Param("days") int days);

    /**
     * 根据标签名精确查询帖子（JSON_CONTAINS匹配）
     * @param tagName 标签名称
     * @param currentUserId 当前用户ID（可为null）
     * @return 帖子列表
     */
    List<Post> findByTagName(@Param("tagName") String tagName, @Param("currentUserId") Long currentUserId);

    /**
     * 根据标签名统计帖子数量
     * @param tagName 标签名称
     * @return 帖子数量
     */
    long countByTagName(@Param("tagName") String tagName);

    /**
     * 检查用户是否点赞了帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否点赞
     */
    boolean existsLike(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 添加点赞记录
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int insertLike(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 删除点赞记录
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int deleteLike(@Param("userId") Long userId, @Param("postId") Long postId);

    // ========== 收藏相关操作 ==========

    /**
     * 检查用户是否收藏了帖子
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否收藏
     */
    boolean existsFavorite(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 添加收藏记录
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int insertFavorite(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 删除收藏记录
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int deleteFavorite(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 增加帖子收藏数
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int incrementFavoriteCount(@Param("postId") Long postId);

    /**
     * 减少帖子收藏数
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int decrementFavoriteCount(@Param("postId") Long postId);

    /**
     * 查询用户收藏的帖子（支持分页）
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> findUserFavorites(@Param("userId") Long userId);

    /**
     * 统计用户收藏的帖子数量
     * @param userId 用户ID
     * @return 收藏数量
     */
    long countUserFavorites(@Param("userId") Long userId);

    /**
     * 查询对当前用户内容的互动列表（赞和收藏，UNION合并按时间倒序）
     * @param userId 当前用户ID
     * @return 互动通知列表
     */
    List<com.icecream.backend.dto.response.InteractionNotification> findInteractionsOnMyContent(@Param("userId") Long userId);

    /**
     * 统计对当前用户内容的互动数量
     * @param userId 当前用户ID
     * @return 互动数量
     */
    long countInteractionsOnMyContent(@Param("userId") Long userId);
}