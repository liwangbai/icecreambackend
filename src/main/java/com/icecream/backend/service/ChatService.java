package com.icecream.backend.service;

import com.icecream.backend.dto.request.ConversationCreateRequest;
import com.icecream.backend.dto.request.MessageSendRequest;
import com.icecream.backend.model.Conversation;
import com.icecream.backend.model.Message;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 获取或创建与某用户的会话
     */
    Conversation getOrCreateConversation(Long userId, Long otherUserId);

    /**
     * 获取当前用户的会话列表
     */
    List<Conversation> getConversations(Long userId);

    /**
     * 获取会话详情
     */
    Conversation getConversationById(Long conversationId, Long userId);

    /**
     * 获取会话的消息列表（分页）
     */
    List<Message> getMessages(Long conversationId, Long userId, int page, int size);

    /**
     * 统计会话的消息数
     */
    long countMessages(Long conversationId);

    /**
     * 发送消息（保存到数据库并推送）
     */
    Message sendMessage(Long conversationId, Long senderId, MessageSendRequest request);

    /**
     * 通过对方用户ID发送消息（自动创建会话）
     */
    Message sendMessageToUser(Long senderId, Long receiverId, MessageSendRequest request);

    /**
     * 撤回消息
     */
    void recallMessage(Long messageId, Long userId);

    /**
     * 标记会话为已读
     */
    void markAsRead(Long conversationId, Long userId);

    /**
     * 获取会话的未读消息数
     */
    long getUnreadCount(Long conversationId, Long userId);

    /**
     * 获取当前用户的所有未读消息总数
     */
    long getTotalUnreadCount(Long userId);
}
