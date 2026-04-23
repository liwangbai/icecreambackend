package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体类
 * 对应数据库中的comments表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity {
    /**
     * 所属帖子ID
     */
    private Long postId;

    /**
     * 评论者用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID（顶级评论为null，支持楼中楼）
     */
    private Long parentId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数（仅一级评论显示）
     */
    private Integer replyCount;

    /**
     * 状态：0-已删除，1-正常
     */
    private Integer status;

    /**
     * 评论作者信息（查询时填充）
     */
    private User author;

    /**
     * 子评论列表（查询时填充，最多显示3条）
     */
    @EqualsAndHashCode.Exclude
    private java.util.List<Comment> replies;

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