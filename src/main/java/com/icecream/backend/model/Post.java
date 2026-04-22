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
     * 门派，例：纯阳
     */
    private String faction;

    /**
     * 大区，例：电信区
     */
    private String region;

    /**
     * 服务器，例：双梦
     */
    private String server;

    /**
     * 体型，例：成男
     */
    private String bodyType;

    /**
     * 玩法，例：PVP
     */
    private String gameplay;

    /**
     * 寻找目标，例：队友
     */
    private String target;

    /**
     * 联系方式具体内容
     */
    private String contactDetail;

    /**
     * 图片链接列表（JSON格式存储）
     */
    private String imageUrls;

    /**
     * 自定义标签列表（JSON格式存储）
     */
    private String tags;

    /**
     * 图片链接列表（查询时从imageUrls JSON解析填充）
     */
    @EqualsAndHashCode.Exclude
    private List<String> imageUrlList;

    /**
     * 标签列表（查询时从tags JSON解析填充）
     */
    @EqualsAndHashCode.Exclude
    private List<String> tagList;

    /**
     * 当前用户是否点赞（需要当前用户ID上下文）
     */
    private Boolean liked;

    /**
     * 获取是否点赞（用于测试兼容性）
     */
    public boolean isLiked() {
        return liked != null && liked;
    }
}