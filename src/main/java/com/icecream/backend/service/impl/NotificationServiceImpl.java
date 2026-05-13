package com.icecream.backend.service.impl;

import com.icecream.backend.dto.response.CommentNotification;
import com.icecream.backend.dto.response.FollowNotification;
import com.icecream.backend.dto.response.InteractionNotification;
import com.icecream.backend.mapper.CommentMapper;
import com.icecream.backend.mapper.PostMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public List<InteractionNotification> getInteractions(Long userId) {
        try {
            return postMapper.findInteractionsOnMyContent(userId);
        } catch (Exception e) {
            log.error("查询互动通知失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public long countInteractions(Long userId) {
        return postMapper.countInteractionsOnMyContent(userId);
    }

    @Override
    public List<CommentNotification> getCommentsAndMentions(Long userId) {
        try {
            return commentMapper.findCommentsAndMentions(userId);
        } catch (Exception e) {
            log.error("查询评论通知失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public long countCommentsAndMentions(Long userId) {
        return commentMapper.countCommentsAndMentions(userId);
    }

    @Override
    public List<FollowNotification> getFollowers(Long userId) {
        try {
            return userMapper.findFollowersWithStatus(userId);
        } catch (Exception e) {
            log.error("查询关注通知失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public long countFollowers(Long userId) {
        return userMapper.countFollowersWithStatus(userId);
    }
}
