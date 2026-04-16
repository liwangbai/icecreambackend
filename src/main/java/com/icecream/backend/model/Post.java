package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子实体类
 * 对应数据库中的posts表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseEntity {
    /**
     * 发布用户ID
     */
    private Long userId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子摘要
     */
    private String summary;

    /**
     * 封面图片URL
     */
    private String coverImageUrl;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 帖子状态：0-草稿，1-已发布，2-已删除
     */
    private Integer status;

    /**
     * 可见性：0-私密，1-公开
     */
    private Integer visibility;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;

    // ========== 关联字段（查询时填充）==========

    /**
     * 发布用户信息
     */
    private User author;

    /**
     * 帖子标签列表
     */
    private List<Tag> tags;

    /**
     * 当前用户是否点赞（需要当前用户ID上下文）
     */
    private Boolean liked;
}