package com.icecream.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 赞和收藏通知DTO
 * 包含帖子点赞、评论点赞、帖子收藏三种类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InteractionNotification {

    /**
     * 互动类型: post_like / comment_like / post_favorite
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
     * 目标ID（被点赞/收藏的帖子ID或评论ID）
     */
    private Long targetId;

    /**
     * 目标标题（帖子标题或评论内容截取）
     */
    private String targetTitle;

    /**
     * 所属帖子ID，用于跳转
     */
    private Long postId;

    /**
     * 互动时间
     */
    private LocalDateTime createdAt;
}
