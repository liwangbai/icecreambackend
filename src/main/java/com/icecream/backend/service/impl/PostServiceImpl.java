package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.PostCreateRequest;
import com.icecream.backend.dto.request.PostQueryRequest;
import com.icecream.backend.dto.request.PostUpdateRequest;
import com.icecream.backend.mapper.PostMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Post;
import com.icecream.backend.model.User;
import com.icecream.backend.service.PostService;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.icecream.backend.exception.ResourceNotFoundException;
import com.icecream.backend.exception.ForbiddenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Post createPost(Long userId, PostCreateRequest request) {
        log.info("创建帖子: userId={}, content={}", userId, request.getContent());

        // 创建帖子对象
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setFaction(request.getFaction());
        post.setRegion(request.getRegion());
        post.setServer(request.getServer());
        post.setBodyType(request.getBodyType());
        post.setGameplay(request.getGameplay());
        post.setTarget(request.getTarget());
        post.setContactDetail(request.getContactDetail());
        // 将图片列表转为JSON字符串存储
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            try {
                post.setImageUrls(objectMapper.writeValueAsString(request.getImageUrls()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("图片链接序列化失败");
            }
        }
        // 设置默认值
        post.setStatus(1);
        post.setVisibility(1);
        post.setIsTop(false);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setPublishedAt(LocalDateTime.now());

        // 插入帖子
        postMapper.insert(post);
        log.info("帖子创建成功: postId={}", post.getId());

        // 更新用户的发帖数
        userMapper.incrementPostCount(userId);

        // 返回创建的帖子
        return getPostById(post.getId(), userId);
    }

    @Override
    @Transactional
    public Post getPostById(Long postId, Long currentUserId) {
        log.debug("获取帖子详情: postId={}, currentUserId={}", postId, currentUserId);

        // 查询帖子基础信息
        Optional<Post> postOpt = postMapper.findById(postId);
        if (!postOpt.isPresent()) {
            throw new ResourceNotFoundException("帖子不存在");
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
            throw new ResourceNotFoundException("帖子不存在");
        }

        Post post = postOpt.get();
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("没有权限更新此帖子");
        }

        // 更新帖子字段
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getFaction() != null) {
            post.setFaction(request.getFaction());
        }
        if (request.getRegion() != null) {
            post.setRegion(request.getRegion());
        }
        if (request.getServer() != null) {
            post.setServer(request.getServer());
        }
        if (request.getBodyType() != null) {
            post.setBodyType(request.getBodyType());
        }
        if (request.getGameplay() != null) {
            post.setGameplay(request.getGameplay());
        }
        if (request.getTarget() != null) {
            post.setTarget(request.getTarget());
        }
        if (request.getContactDetail() != null) {
            post.setContactDetail(request.getContactDetail());
        }
        if (request.getImageUrls() != null) {
            try {
                post.setImageUrls(objectMapper.writeValueAsString(request.getImageUrls()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("图片链接序列化失败");
            }
        }

        // 更新数据库
        postMapper.update(post);

        return getPostById(postId, userId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        log.info("删除帖子: postId={}, userId={}", postId, userId);

        Optional<Post> postOpt = postMapper.findById(postId);
        if (!postOpt.isPresent()) {
            throw new ResourceNotFoundException("帖子不存在");
        }

        Post post = postOpt.get();
        if (!post.getUserId().equals(userId)) {
            throw new ForbiddenException("没有权限删除此帖子");
        }

        postMapper.delete(postId);
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
     * 丰富帖子信息（包含作者、标签等关联信息）
     */
    private void enrichPostWithAssociations(Post post, Long currentUserId) {
        // 设置作者信息
        Optional<User> userOpt = userMapper.findById(post.getUserId());
        userOpt.ifPresent(post::setAuthor);

        // 将imageUrls从JSON字符串转为List
        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            try {
                post.setImageUrlList(objectMapper.readValue(post.getImageUrls(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            } catch (JsonProcessingException e) {
                log.warn("解析图片链接失败: {}", post.getImageUrls());
            }
        }

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