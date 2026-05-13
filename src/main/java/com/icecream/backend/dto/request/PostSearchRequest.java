package com.icecream.backend.dto.request;

import lombok.Data;

/**
 * 帖子关键词搜索请求类
 * 用于根据关键词搜索帖子，支持分页和可选的筛选条件
 */
@Data
public class PostSearchRequest {
    /**
     * 搜索关键词（必填，1-100字符）
     */
    private String keyword;

    /**
     * 页码，从0开始
     */
    private Integer page = 0;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 门派筛选
     */
    private String faction;

    /**
     * 大区筛选
     */
    private String region;

    /**
     * 服务器筛选
     */
    private String server;

    /**
     * 体型筛选
     */
    private String bodyType;

    /**
     * 玩法筛选
     */
    private String gameplay;

    /**
     * 当前用户ID（服务端设置，不从客户端传入）
     */
    private Long currentUserId;
}
