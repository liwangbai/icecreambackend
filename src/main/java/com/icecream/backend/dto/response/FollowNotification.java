package com.icecream.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新增关注通知DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FollowNotification {

    /**
     * 关注者用户ID
     */
    private Long userId;

    /**
     * 关注者昵称
     */
    private String nickname;

    /**
     * 关注者头像URL
     */
    private String avatarUrl;

    /**
     * 关注时间
     */
    private LocalDateTime followedAt;

    /**
     * 当前用户是否回关了该关注者
     */
    private Boolean followedBack;
}
