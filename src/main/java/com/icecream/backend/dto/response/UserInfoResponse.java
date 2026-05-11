package com.icecream.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应DTO
 * 用于关注列表和粉丝列表，包含互关状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoResponse {

    private Long id;

    private String username;

    private String nickname;

    private String avatarUrl;

    private String bio;

    private Integer gender;

    private Integer followerCount;

    private Integer followingCount;

    private Integer postCount;

    private Integer likeCount;

    /** 当前用户是否与该用户互相关注 */
    private Boolean followedBack;
}
