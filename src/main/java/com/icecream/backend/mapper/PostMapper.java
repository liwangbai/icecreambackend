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
}