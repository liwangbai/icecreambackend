package com.icecream.backend.service.impl;

import com.github.pagehelper.PageHelper;
import com.icecream.backend.dto.request.MessageSendRequest;
import com.icecream.backend.exception.ForbiddenException;
import com.icecream.backend.exception.ResourceNotFoundException;
import com.icecream.backend.mapper.ConversationMapper;
import com.icecream.backend.mapper.MessageMapper;
import com.icecream.backend.mapper.MessageReadMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Conversation;
import com.icecream.backend.model.Message;
import com.icecream.backend.model.MessageRead;
import com.icecream.backend.model.User;
import com.icecream.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final MessageReadMapper messageReadMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public Conversation getOrCreateConversation(Long userId, Long otherUserId) {
        log.info("获取或创建会话: userId={}, otherUserId={}", userId, otherUserId);

        // 验证对方用户存在
        Optional<User> otherUserOpt = userMapper.findById(otherUserId);
        if (!otherUserOpt.isPresent()) {
            throw new ResourceNotFoundException("对方用户不存在");
        }

        // 确保user1Id < user2Id（保证唯一性）
        Long user1Id = Math.min(userId, otherUserId);
        Long user2Id = Math.max(userId, otherUserId);

        // 查找现有会话
        Optional<Conversation> existing = conversationMapper.findByUserIds(user1Id, user2Id);
        if (existing.isPresent()) {
            Conversation conv = existing.get();
            fillOtherUser(conv, userId);
            fillUnreadCount(conv, userId);
            return conv;
        }

        // 创建新会话
        Conversation conversation = new Conversation();
        conversation.setUser1Id(user1Id);
        conversation.setUser2Id(user2Id);
        conversationMapper.insert(conversation);

        // 初始化双方的已读状态
        MessageRead read1 = new MessageRead();
        read1.setConversationId(conversation.getId());
        read1.setUserId(user1Id);
        read1.setLastReadMessageId(0L);
        read1.setLastReadAt(LocalDateTime.now());
        messageReadMapper.insertOrUpdate(read1);

        MessageRead read2 = new MessageRead();
        read2.setConversationId(conversation.getId());
        read2.setUserId(user2Id);
        read2.setLastReadMessageId(0L);
        read2.setLastReadAt(LocalDateTime.now());
        messageReadMapper.insertOrUpdate(read2);

        fillOtherUser(conversation, userId);
        conversation.setUnreadCount(0);
        return conversation;
    }

    @Override
    public List<Conversation> getConversations(Long userId) {
        log.debug("获取会话列表: userId={}", userId);
        List<Conversation> conversations = conversationMapper.findByUserId(userId);
        if (!conversations.isEmpty()) {
            fillUnreadCounts(conversations, userId);
        }
        return conversations;
    }

    @Override
    public Conversation getConversationById(Long conversationId, Long userId) {
        log.debug("获取会话详情: conversationId={}, userId={}", conversationId, userId);
        Optional<Conversation> convOpt = conversationMapper.findById(conversationId);
        if (!convOpt.isPresent()) {
            throw new ResourceNotFoundException("会话不存在");
        }
        Conversation conv = convOpt.get();
        if (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId)) {
            throw new ForbiddenException("无权访问此会话");
        }
        fillOtherUser(conv, userId);
        fillUnreadCount(conv, userId);
        return conv;
    }

    @Override
    public List<Message> getMessages(Long conversationId, Long userId, int page, int size) {
        log.debug("获取消息列表: conversationId={}, userId={}, page={}, size={}", conversationId, userId, page, size);

        // 验证会话权限
        getConversationById(conversationId, userId);

        PageHelper.startPage(page + 1, size);
        return messageMapper.findByConversationId(conversationId);
    }

    @Override
    public long countMessages(Long conversationId) {
        return messageMapper.countByConversationId(conversationId);
    }

    @Override
    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, MessageSendRequest request) {
        log.info("发送消息: conversationId={}, senderId={}", conversationId, senderId);

        // 验证会话权限
        Conversation conversation = getConversationById(conversationId, senderId);

        // 创建消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : 1);
        message.setStatus(1);
        messageMapper.insert(message);

        // 更新会话的最后消息
        conversationMapper.updateLastMessage(conversationId, message.getId(), message.getCreatedAt());

        // 查询完整消息信息（包含发送者）
        Message fullMessage = messageMapper.findById(message.getId()).orElse(message);

        // WebSocket推送给接收者
        Long receiverId = conversation.getUser1Id().equals(senderId) ? conversation.getUser2Id() : conversation.getUser1Id();
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                fullMessage
        );
        log.debug("消息已推送: receiverId={}", receiverId);

        return fullMessage;
    }

    @Override
    @Transactional
    public Message sendMessageToUser(Long senderId, Long receiverId, MessageSendRequest request) {
        log.info("发送消息给用户: senderId={}, receiverId={}", senderId, receiverId);
        Conversation conversation = getOrCreateConversation(senderId, receiverId);
        return sendMessage(conversation.getId(), senderId, request);
    }

    @Override
    @Transactional
    public void recallMessage(Long messageId, Long userId) {
        log.info("撤回消息: messageId={}, userId={}", messageId, userId);

        Optional<Message> msgOpt = messageMapper.findById(messageId);
        if (!msgOpt.isPresent()) {
            throw new ResourceNotFoundException("消息不存在");
        }

        Message message = msgOpt.get();
        if (!message.getSenderId().equals(userId)) {
            throw new ForbiddenException("无权撤回此消息");
        }

        // 检查是否已超过撤回时间（2分钟内可撤回）
        if (message.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(2))) {
            throw new ForbiddenException("消息已超过撤回时间");
        }

        // 软删除消息
        messageMapper.softDelete(messageId);

        // WebSocket通知对方
        Conversation conversation = conversationMapper.findById(message.getConversationId()).orElse(null);
        if (conversation != null) {
            Long receiverId = conversation.getUser1Id().equals(userId) ? conversation.getUser2Id() : conversation.getUser1Id();
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/system",
                    java.util.Map.of("type", "message_recall", "messageId", messageId)
            );
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        log.info("标记已读: conversationId={}, userId={}", conversationId, userId);

        // 验证会话权限
        getConversationById(conversationId, userId);

        // 获取会话的最新消息ID
        Conversation conv = conversationMapper.findById(conversationId).orElse(null);
        if (conv != null && conv.getLastMessageId() != null) {
            messageReadMapper.updateLastRead(conversationId, userId, conv.getLastMessageId());
        }
    }

    @Override
    public long getUnreadCount(Long conversationId, Long userId) {
        // 验证会话权限
        getConversationById(conversationId, userId);

        Optional<MessageRead> readOpt = messageReadMapper.findByConversationAndUser(conversationId, userId);
        long lastReadMessageId = readOpt.map(MessageRead::getLastReadMessageId).orElse(0L);

        return messageMapper.countUnread(conversationId, userId, lastReadMessageId);
    }

    @Override
    public long getTotalUnreadCount(Long userId) {
        List<Conversation> conversations = conversationMapper.findByUserId(userId);
        if (conversations.isEmpty()) {
            return 0;
        }
        List<Long> conversationIds = conversations.stream()
                .map(Conversation::getId).collect(Collectors.toList());
        Map<Long, Long> unreadMap = getUnreadCountBatch(conversationIds, userId);
        return unreadMap.values().stream().mapToLong(Long::longValue).sum();
    }

    // ========== 私有方法 ==========

    /**
     * 填充对方用户信息
     */
    private void fillOtherUser(Conversation conversation, Long currentUserId) {
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
        Optional<User> otherUserOpt = userMapper.findById(otherUserId);
        if (otherUserOpt.isPresent()) {
            conversation.setOtherUser(otherUserOpt.get());
        }
    }

    /**
     * 填充未读消息数（单条，用于详情接口）
     */
    private void fillUnreadCount(Conversation conversation, Long userId) {
        Optional<MessageRead> readOpt = messageReadMapper.findByConversationAndUser(conversation.getId(), userId);
        long lastReadMessageId = readOpt.map(MessageRead::getLastReadMessageId).orElse(0L);
        long unreadCount = messageMapper.countUnread(conversation.getId(), userId, lastReadMessageId);
        conversation.setUnreadCount((int) unreadCount);
    }

    /**
     * 批量填充未读消息数
     */
    private void fillUnreadCounts(List<Conversation> conversations, Long userId) {
        List<Long> conversationIds = conversations.stream()
                .map(Conversation::getId).collect(Collectors.toList());
        Map<Long, Long> unreadMap = getUnreadCountBatch(conversationIds, userId);
        for (Conversation conv : conversations) {
            conv.setUnreadCount(unreadMap.getOrDefault(conv.getId(), 0L).intValue());
        }
    }

    /**
     * 批量查询多个会话的未读消息数，一次SQL完成
     */
    private Map<Long, Long> getUnreadCountBatch(List<Long> conversationIds, Long userId) {
        List<Map<String, Object>> rows = messageReadMapper.countUnreadBatch(conversationIds, userId);
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long convId = ((Number) row.get("conversation_id")).longValue();
            Long count = ((Number) row.get("unread_count")).longValue();
            result.put(convId, count);
        }
        return result;
    }
}
