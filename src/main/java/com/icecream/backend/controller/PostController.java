package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.HotTagDTO;
import com.icecream.backend.dto.PagedResult;
import com.icecream.backend.dto.request.PostCreateRequest;
import com.icecream.backend.dto.request.PostQueryRequest;
import com.icecream.backend.dto.request.PostUpdateRequest;
import com.icecream.backend.model.Post;
import com.icecream.backend.service.PostService;
import com.icecream.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.pagehelper.Page;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 帖子控制器
 * 处理帖子相关的所有操作，包括创建、查询、更新、删除、点赞等
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子相关操作接口，包括创建、查询、更新、删除、点赞等")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "创建帖子", description = "创建新帖子，必填字段：content、faction、region、server、bodyType、gameplay")
    public ResponseEntity<ApiResponse<Post>> createPost(@Valid @RequestBody PostCreateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("创建帖子: userId={}, title={}", currentUserId, request.getTitle());
        Post post = postService.createPost(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("帖子创建成功", post));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "获取帖子详情", description = "根据ID获取帖子详情，同时增加帖子浏览数")
    public ResponseEntity<ApiResponse<Post>> getPostById(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        log.debug("获取帖子详情: postId={}, userId={}", postId, currentUserId);
        Post post = postService.getPostById(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", post));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "更新帖子", description = "更新帖子信息，仅作者或管理员可以更新")
    public ResponseEntity<ApiResponse<Post>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("更新帖子: postId={}, userId={}", postId, currentUserId);
        Post post = postService.updatePost(postId, currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("帖子更新成功", post));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "删除帖子", description = "删除帖子，仅作者或管理员可以删除")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("删除帖子: postId={}, userId={}", postId, currentUserId);
        postService.deletePost(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("帖子删除成功"));
    }

    @GetMapping
    @Operation(summary = "查询帖子列表", description = "查询帖子列表，支持分页、排序和筛选条件（门派、大区、服务器、体型、玩法、标签）")
    public ResponseEntity<ApiResponse<PagedResult<Post>>> queryPosts(
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long userId,
            @Parameter(description = "门派筛选") @RequestParam(required = false) String faction,
            @Parameter(description = "大区筛选") @RequestParam(required = false) String region,
            @Parameter(description = "服务器筛选") @RequestParam(required = false) String server,
            @Parameter(description = "体型筛选") @RequestParam(required = false) String bodyType,
            @Parameter(description = "玩法筛选") @RequestParam(required = false) String gameplay,
            @Parameter(description = "标签筛选（模糊匹配）") @RequestParam(required = false) String tag,
            @Parameter(description = "排序字段: publishedAt, viewCount, likeCount, hot")
            @RequestParam(required = false, defaultValue = "publishedAt") String sortBy,
            @Parameter(description = "排序方向: asc, desc")
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();

        // 构建查询条件
        PostQueryRequest query = new PostQueryRequest();
        query.setPage(page);
        query.setSize(size);
        query.setUserId(userId);
        query.setFaction(faction);
        query.setRegion(region);
        query.setServer(server);
        query.setBodyType(bodyType);
        query.setGameplay(gameplay);
        query.setTag(tag);
        query.setSortBy(sortBy);
        query.setSortDirection(sortDirection);
        query.setCurrentUserId(currentUserId);

        log.debug("查询帖子列表: {}", query);
        List<Post> posts = postService.queryPosts(query);
        long total = postService.countPosts(query);

        PagedResult<Post> pagedResult = PagedResult.of(posts, total, page, size);
        ApiResponse<PagedResult<Post>> response = ApiResponse.success("查询成功", pagedResult);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "点赞帖子", description = "给指定帖子点赞")
    public ResponseEntity<ApiResponse<Void>> likePost(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("点赞帖子: postId={}, userId={}", postId, currentUserId);
        postService.likePost(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("点赞成功"));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "取消点赞", description = "取消对指定帖子的点赞")
    public ResponseEntity<ApiResponse<Void>> unlikePost(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("取消点赞: postId={}, userId={}", postId, currentUserId);
        postService.unlikePost(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功"));
    }

    @GetMapping("/{postId}/is-liked")
    @Operation(summary = "检查是否点赞", description = "检查当前用户是否点赞了指定帖子")
    public ResponseEntity<ApiResponse<Boolean>> isLiked(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("检查是否点赞: postId={}, userId={}", postId, currentUserId);
        boolean isLiked = postService.isLiked(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("检查成功", isLiked));
    }

    @PostMapping("/{postId}/favorite")
    @Operation(summary = "收藏帖子", description = "收藏指定帖子")
    public ResponseEntity<ApiResponse<Void>> favoritePost(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("收藏帖子: postId={}, userId={}", postId, currentUserId);
        postService.favoritePost(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("收藏成功"));
    }

    @DeleteMapping("/{postId}/favorite")
    @Operation(summary = "取消收藏", description = "取消对指定帖子的收藏")
    public ResponseEntity<ApiResponse<Void>> unfavoritePost(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("取消收藏: postId={}, userId={}", postId, currentUserId);
        postService.unfavoritePost(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("取消收藏成功"));
    }

    @GetMapping("/{postId}/is-favorited")
    @Operation(summary = "检查是否收藏", description = "检查当前用户是否收藏了指定帖子")
    public ResponseEntity<ApiResponse<Boolean>> isFavorited(@PathVariable Long postId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("检查是否收藏: postId={}, userId={}", postId, currentUserId);
        boolean isFavorited = postService.isFavorited(postId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("检查成功", isFavorited));
    }

    @GetMapping("/favorites")
    @Operation(summary = "获取收藏列表", description = "获取当前用户收藏的帖子列表，支持分页")
    public ResponseEntity<ApiResponse<PagedResult<Post>>> getUserFavorites(
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取用户收藏列表: userId={}, page={}, size={}", currentUserId, page, size);

        // 使用PageHelper分页
        com.github.pagehelper.PageHelper.startPage(page + 1, size);
        List<Post> posts = postService.getUserFavorites(currentUserId);
        long total = postService.countUserFavorites(currentUserId);

        PagedResult<Post> pagedResult = PagedResult.of(posts, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户帖子", description = "获取指定用户发布的帖子，支持分页")
    public ResponseEntity<ApiResponse<PagedResult<Post>>> getUserPosts(
            @PathVariable Long userId,
            @Parameter(description = "帖子状态: 0-草稿, 1-已发布, 2-已删除")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.debug("获取用户帖子: userId={}, status={}, page={}, size={}", userId, status, page, size);
        com.github.pagehelper.PageHelper.startPage(page + 1, size);
        List<Post> posts = postService.getUserPosts(userId, status);
        long total = ((Page) posts).getTotal();
        PagedResult<Post> pagedResult = PagedResult.of(posts, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/following")
    @Operation(summary = "获取关注用户帖子", description = "获取当前用户关注的用户发布的帖子（时间线），支持分页")
    public ResponseEntity<ApiResponse<PagedResult<Post>>> getFollowingPosts(
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取关注用户帖子: userId={}, page={}, size={}", currentUserId, page, size);
        com.github.pagehelper.PageHelper.startPage(page + 1, size);
        List<Post> posts = postService.getFollowingPosts(currentUserId);
        long total = ((Page) posts).getTotal();
        PagedResult<Post> pagedResult = PagedResult.of(posts, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @GetMapping("/hot-tags")
    @Operation(summary = "获取热门标签", description = "获取最近30天内最热门的9个自定义标签")
    public ResponseEntity<ApiResponse<List<HotTagDTO>>> getHotTags() {
        log.debug("获取热门标签");
        List<HotTagDTO> hotTags = postService.getHotTags(30, 9);
        return ResponseEntity.ok(ApiResponse.success("获取成功", hotTags));
    }

    @GetMapping("/by-tag/{tagName}")
    @Operation(summary = "获取标签下的帖子", description = "根据标签名精确查询所有包含该标签的帖子，支持分页")
    public ResponseEntity<ApiResponse<PagedResult<Post>>> getPostsByTagName(
            @Parameter(description = "标签名称") @PathVariable String tagName,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "排序字段: publishedAt, viewCount, likeCount, hot")
            @RequestParam(required = false, defaultValue = "publishedAt") String sortBy,
            @Parameter(description = "排序方向: asc, desc")
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        Long currentUserId = SecurityUtil.getCurrentUserIdOrNull();
        log.debug("获取标签下的帖子: tagName={}, page={}, size={}", tagName, page, size);

        // 使用PageHelper分页
        com.github.pagehelper.PageHelper.startPage(page + 1, size);
        List<Post> posts = postService.getPostsByTagName(tagName, currentUserId);
        long total = postService.countPostsByTagName(tagName);

        PagedResult<Post> pagedResult = PagedResult.of(posts, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

}