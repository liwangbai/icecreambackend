package com.icecream.backend.service;

import com.icecream.backend.dto.response.CommentNotification;
import com.icecream.backend.dto.response.FollowNotification;
import com.icecream.backend.dto.response.InteractionNotification;

import java.util.List;

/**
 * 通知服务接口
 * 负责查询用户的互动通知、评论通知、关注通知
 */
public interface NotificationService {

    /**
     * 查询对当前用户内容的互动列表（赞和收藏）
     * @param userId 当前用户ID
     * @return 互动通知列表
     */
    List<InteractionNotification> getInteractions(Long userId);

    /**
     * 统计互动数量
     * @param userId 当前用户ID
     * @return 互动数量
     */
    long countInteractions(Long userId);

    /**
     * 查询评论和@通知列表
     * @param userId 当前用户ID
     * @return 评论通知列表
     */
    List<CommentNotification> getCommentsAndMentions(Long userId);

    /**
     * 统计评论和@通知数量
     * @param userId 当前用户ID
     * @return 通知数量
     */
    long countCommentsAndMentions(Long userId);

    /**
     * 查询新增关注列表（带是否回关标志）
     * @param userId 当前用户ID
     * @return 关注通知列表
     */
    List<FollowNotification> getFollowers(Long userId);

    /**
     * 统计粉丝数量
     * @param userId 当前用户ID
     * @return 粉丝数量
     */
    long countFollowers(Long userId);
}
