package com.icecream.backend.service;

import com.icecream.backend.dto.HotTagDTO;
import com.icecream.backend.dto.request.PostCreateRequest;
import com.icecream.backend.dto.request.PostQueryRequest;
import com.icecream.backend.dto.request.PostUpdateRequest;
import com.icecream.backend.model.Post;

import java.util.List;

/**
 * 帖子服务接口
 * 负责帖子管理、查询、点赞等核心业务功能
 */
public interface PostService {

    /**
     * 创建新帖子
     * 帖子必须包含至少一个标签
     * @param userId 发布用户ID
     * @param request 创建请求
     * @return 创建的帖子
     */
    Post createPost(Long userId, PostCreateRequest request);

    /**
     * 根据ID获取帖子详情
     * 增加浏览数
     * @param postId 帖子ID
     * @param currentUserId 当前用户ID（用于判断是否点赞，可为null）
     * @return 帖子详情
     */
    Post getPostById(Long postId, Long currentUserId);

    /**
     * 更新帖子
     * 仅作者或管理员可以更新
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param request 更新请求
     * @return 更新后的帖子
     */
    Post updatePost(Long postId, Long userId, PostUpdateRequest request);

    /**
     * 删除帖子
     * 仅作者或管理员可以删除
     * @param postId 帖子ID
     * @param userId 当前用户ID
     */
    void deletePost(Long postId, Long userId);

    /**
     * 查询帖子列表（支持条件查询）
     * @param query 查询条件
     * @return 帖子列表
     */
    List<Post> queryPosts(PostQueryRequest query);

    /**
     * 统计符合条件的帖子数量
     * @param query 查询条件
     * @return 帖子数量
     */
    long countPosts(PostQueryRequest query);

    /**
     * 点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void likePost(Long postId, Long userId);

    /**
     * 取消点赞
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void unlikePost(Long postId, Long userId);

    /**
     * 检查用户是否点赞了帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否点赞
     */
    boolean isLiked(Long postId, Long userId);

    /**
     * 收藏帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void favoritePost(Long postId, Long userId);

    /**
     * 取消收藏
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void unfavoritePost(Long postId, Long userId);

    /**
     * 检查用户是否收藏了帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否收藏
     */
    boolean isFavorited(Long postId, Long userId);

    /**
     * 获取用户收藏的帖子列表
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> getUserFavorites(Long userId);

    /**
     * 统计用户收藏的帖子数量
     * @param userId 用户ID
     * @return 收藏数量
     */
    long countUserFavorites(Long userId);

    /**
     * 查询用户发布的帖子
     * @param userId 用户ID
     * @param status 帖子状态（可选，null表示所有状态）
     * @return 帖子列表
     */
    List<Post> getUserPosts(Long userId, Integer status);

    /**
     * 查询用户关注的用户的帖子（时间线）
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> getFollowingPosts(Long userId);

    /**
     * 根据标签ID查询帖子
     * @param tagId 标签ID
     * @return 帖子列表
     */
    List<Post> getPostsByTagId(Long tagId);

    /**
     * 获取热门自定义标签
     * @param days 统计天数
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    List<HotTagDTO> getHotTags(int days, int limit);

    /**
     * 根据标签名查询帖子（精确匹配JSON数组中的标签）
     * @param tagName 标签名称
     * @param currentUserId 当前用户ID（可为null）
     * @return 帖子列表
     */
    List<Post> getPostsByTagName(String tagName, Long currentUserId);

    /**
     * 根据标签名统计帖子数量
     * @param tagName 标签名称
     * @return 帖子数量
     */
    long countPostsByTagName(String tagName);
}