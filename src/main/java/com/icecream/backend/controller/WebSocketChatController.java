package com.icecream.backend.controller;

import com.icecream.backend.dto.request.MessageSendRequest;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Message;
import com.icecream.backend.model.User;
import com.icecream.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

/**
 * WebSocket STOMP 消息处理器
 * 客户端通过 STOMP 发送消息到 /app/chat.send
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;
    private final UserMapper userMapper;

    /**
     * 处理客户端通过 WebSocket 发送的消息
     * 客户端发送到 /app/chat.send
     * WebSocket 消息在 executor 线程池执行，不能依赖线程局部的 SecurityContextHolder，
     * 需通过 STOMP 会话中的 Principal 获取用户身份
     */
    @MessageMapping("/chat.send")
    @SendToUser("/queue/ack")
    public Message sendMessage(@Payload ChatMessagePayload payload, Principal principal) {
        String username = principal.getName();
        Optional<User> userOpt = userMapper.findByUsername(username);
        if (!userOpt.isPresent()) {
            log.error("WebSocket发送消息: 用户不存在, username={}", username);
            throw new IllegalStateException("用户不存在: " + username);
        }
        Long senderId = userOpt.get().getId();
        log.info("WebSocket发送消息: senderId={}, conversationId={}", senderId, payload.getConversationId());

        MessageSendRequest request = new MessageSendRequest();
        request.setContent(payload.getContent());
        request.setType(payload.getType());

        return chatService.sendMessage(payload.getConversationId(), senderId, request);
    }

    /**
     * 客户端应用层心跳，STOMP 协议层心跳由 broker 自动维护
     */
    @MessageMapping("/heartbeat")
    public void heartbeat() {
    }

    /**
     * WebSocket 消息载体
     */
    @lombok.Data
    public static class ChatMessagePayload {
        private Long conversationId;
        private String content;
        private Integer type;
    }
}
