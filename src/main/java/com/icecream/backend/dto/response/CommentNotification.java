package com.icecream.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论和@通知DTO
 * 包含评论我的帖子和@我的回复两种类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentNotification {

    /**
     * 类型: comment（评论了我的帖子） / reply（回复/@了我）
     */
    private String type;

    /**
     * 操作者用户ID
     */
    private Long fromUserId;

    /**
     * 操作者昵称
     */
    private String fromNickname;

    /**
     * 操作者头像URL
     */
    private String fromAvatarUrl;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 帖子标题
     */
    private String postTitle;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 评论内容（截取前100字）
     */
    private String commentContent;

    /**
     * 评论时间
     */
    private LocalDateTime createdAt;
}
