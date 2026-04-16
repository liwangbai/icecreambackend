package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.PostCreateRequest;
import com.icecream.backend.dto.request.PostQueryRequest;
import com.icecream.backend.dto.request.PostUpdateRequest;
import com.icecream.backend.mapper.PostMapper;
import com.icecream.backend.mapper.TagMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Post;
import com.icecream.backend.model.Tag;
import com.icecream.backend.model.User;
import com.icecream.backend.service.PostService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 帖子服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Post createPost(Long userId, PostCreateRequest request) {
        log.info("创建帖子: userId={}, title={}", userId, request.getTitle());

        // 验证标签是否存在
        validateTags(request.getTagIds());

        // 创建帖子对象
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setCoverImageUrl(request.getCoverImageUrl());
        post.setStatus(request.getStatus());
        post.setVisibility(request.getVisibility());
        post.setIsTop(request.getIsTop());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setPublishedAt(LocalDateTime.now());

        // 插入帖子
        postMapper.insert(post);
        log.info("帖子创建成功: postId={}", post.getId());

        // 添加帖子标签关联
        for (Long tagId : request.getTagIds()) {
            tagMapper.insertPostTag(post.getId(), tagId);
            tagMapper.incrementUseCount(tagId); // 增加标签使用次数
        }

        // 更新用户的发帖数
        userMapper.incrementPostCount(userId);

        // 返回创建的帖子（包含关联信息）
        return getPostById(post.getId(), userId);
    }

    @Override
    @Transactional
    public Post getPostById(Long postId, Long currentUserId) {
        log.debug("获取帖子详情: postId={}, currentUserId={}", postId, currentUserId);

        // 查询帖子基础信息
        Optional<Post> postOpt = postMapper.findById(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("帖子不存在: " + postId);
        }

        Post post = postOpt.get();

        // 增加浏览数
        postMapper.incrementViewCount(postId);

        // 设置关联信息
        enrichPostWithAssociations(post, currentUserId);

        return post;
    }

    @Override
    @Transactional
    public Post updatePost(Long postId, Long userId, PostUpdateRequest request) {
        log.info("更新帖子: postId={}, userId={}", postId, userId);

        // 检查帖子是否存在且用户有权限
        Optional<Post> postOpt = postMapper.findById(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("帖子不存在: " + postId);
        }

        Post post = postOpt.get();
        if (!post.getUserId().equals(userId)) {
            // TODO: 检查是否为管理员
            throw new RuntimeException("没有权限更新此帖子");
        }

        // 更新帖子字段
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            post.setSummary(request.getSummary());
        }
        if (request.getCoverImageUrl() != null) {
            post.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        if (request.getIsTop() != null) {
            post.setIsTop(request.getIsTop());
        }

        // 更新数据库
        postMapper.update(post);

        // 如果提供了新的标签列表，更新标签关联
        if (request.getTagIds() != null) {
            // 验证新标签
            validateTags(request.getTagIds());

            // 删除旧的标签关联
            tagMapper.deletePostTagsByPostId(postId);

            // 添加新的标签关联
            for (Long tagId : request.getTagIds()) {
                tagMapper.insertPostTag(postId, tagId);
                tagMapper.incrementUseCount(tagId);
            }
        }

        return getPostById(postId, userId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        log.info("删除帖子: postId={}, userId={}", postId, userId);

        // 检查帖子是否存在且用户有权限
        Optional<Post> postOpt = postMapper.findById(postId);
        if (!postOpt.isPresent()) {
            throw new RuntimeException("帖子不存在: " + postId);
        }

        Post post = postOpt.get();
        if (!post.getUserId().equals(userId)) {
            // TODO: 检查是否为管理员
            throw new RuntimeException("没有权限删除此帖子");
        }

        // 删除帖子（物理删除或逻辑删除）
        // 这里使用物理删除，实际项目中可能需要逻辑删除
        postMapper.delete(postId);

        // 减少用户的发帖数
        userMapper.decrementPostCount(userId);

        log.info("帖子删除成功: postId={}", postId);
    }

    @Override
    public List<Post> queryPosts(PostQueryRequest query) {
        log.debug("查询帖子列表: {}", query);

        // 设置分页
        PageHelper.startPage(query.getPage(), query.getSize());

        // 执行查询
        List<Post> posts = postMapper.findByCondition(query);

        // 为每个帖子设置关联信息
        for (Post post : posts) {
            enrichPostWithAssociations(post, query.getCurrentUserId());
        }

        return posts;
    }

    @Override
    public long countPosts(PostQueryRequest query) {
        return postMapper.countByCondition(query);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        log.info("点赞帖子: postId={}, userId={}", postId, userId);

        // 检查是否已点赞
        if (postMapper.existsLike(userId, postId)) {
            throw new RuntimeException("已经点赞过此帖子");
        }

        // 添加点赞记录
        postMapper.insertLike(userId, postId);

        // 增加帖子点赞数
        postMapper.incrementLikeCount(postId);
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        log.info("取消点赞: postId={}, userId={}", postId, userId);

        // 检查是否已点赞
        if (!postMapper.existsLike(userId, postId)) {
            throw new RuntimeException("尚未点赞此帖子");
        }

        // 删除点赞记录
        postMapper.deleteLike(userId, postId);

        // 减少帖子点赞数
        postMapper.decrementLikeCount(postId);
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        return postMapper.existsLike(userId, postId);
    }

    @Override
    public List<Post> getUserPosts(Long userId, Integer status) {
        log.debug("查询用户帖子: userId={}, status={}", userId, status);

        List<Post> posts = postMapper.findByUserId(userId, status);

        // 为每个帖子设置基本关联信息
        for (Post post : posts) {
            enrichPostWithBasicAssociations(post, userId);
        }

        return posts;
    }

    @Override
    public List<Post> getFollowingPosts(Long userId) {
        log.debug("查询关注用户帖子: userId={}", userId);

        List<Post> posts = postMapper.findFollowingPosts(userId);

        // 为每个帖子设置基本关联信息
        for (Post post : posts) {
            enrichPostWithBasicAssociations(post, userId);
        }

        return posts;
    }

    @Override
    public List<Post> getPostsByTagId(Long tagId) {
        log.debug("根据标签查询帖子: tagId={}", tagId);

        List<Post> posts = postMapper.findByTagId(tagId);

        // 为每个帖子设置基本关联信息
        for (Post post : posts) {
            enrichPostWithBasicAssociations(post, null);
        }

        return posts;
    }

    // ========== 私有方法 ==========

    /**
     * 验证标签ID列表是否有效
     */
    private void validateTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new RuntimeException("帖子必须包含至少一个标签");
        }

        for (Long tagId : tagIds) {
            Optional<Tag> tagOpt = tagMapper.findById(tagId);
            if (!tagOpt.isPresent() || !tagOpt.get().getIsActive()) {
                throw new RuntimeException("标签不存在或未启用: " + tagId);
            }
        }
    }

    /**
     * 丰富帖子信息（包含作者、标签等关联信息）
     */
    private void enrichPostWithAssociations(Post post, Long currentUserId) {
        // 设置作者信息
        Optional<User> userOpt = userMapper.findById(post.getUserId());
        userOpt.ifPresent(post::setAuthor);

        // 设置标签信息
        List<Tag> tags = tagMapper.findTagsByPostId(post.getId());
        post.setTags(tags);

        // 设置是否点赞
        if (currentUserId != null) {
            post.setLiked(postMapper.existsLike(currentUserId, post.getId()));
        } else {
            post.setLiked(false);
        }
    }

    /**
     * 丰富帖子基本信息（仅包含作者基本信息）
     */
    private void enrichPostWithBasicAssociations(Post post, Long currentUserId) {
        // 设置作者基本信息
        Optional<User> userOpt = userMapper.findById(post.getUserId());
        if (userOpt.isPresent()) {
            User author = new User();
            author.setId(userOpt.get().getId());
            author.setUsername(userOpt.get().getUsername());
            author.setNickname(userOpt.get().getNickname());
            author.setAvatarUrl(userOpt.get().getAvatarUrl());
            post.setAuthor(author);
        }

        // 设置是否点赞
        if (currentUserId != null) {
            post.setLiked(postMapper.existsLike(currentUserId, post.getId()));
        } else {
            post.setLiked(false);
        }
    }
}