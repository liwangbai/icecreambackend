package com.icecream.backend.dto.request;

import lombok.Data;

/**
 * 帖子查询条件请求类
 * 用于帖子列表的条件查询，支持分页、筛选和排序
 */
@Data
public class PostQueryRequest {
    /**
     * 页码，从0开始
     */
    private Integer page = 0;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 用户ID筛选条件，查询指定用户的帖子
     */
    private Long userId;

    /**
     * 标签ID筛选条件，查询包含指定标签的帖子
     */
    private Long tagId;

    /**
     * 排序字段，可选值：publishedAt, viewCount, likeCount
     */
    private String sortBy = "publishedAt";

    /**
     * 排序方向，可选值：asc（升序）, desc（降序）
     */
    private String sortDirection = "desc";

    /**
     * 帖子状态筛选，默认只查询已发布的帖子
     * 0-草稿，1-已发布，2-已删除
     */
    private Integer status = 1;

    /**
     * 可见性筛选，默认只查询公开的帖子
     * 0-私密，1-公开
     */
    private Integer visibility = 1;

    /**
     * 当前用户ID（用于查询用户是否点赞等关联信息）
     * 注意：这个字段不会从客户端传入，由服务端设置
     */
    private Long currentUserId;
}