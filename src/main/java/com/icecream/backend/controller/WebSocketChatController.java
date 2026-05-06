package com.icecream.backend.controller;

import com.icecream.backend.dto.request.MessageSendRequest;
import com.icecream.backend.model.Message;
import com.icecream.backend.service.ChatService;
import com.icecream.backend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket STOMP 消息处理器
 * 客户端通过 STOMP 发送消息到 /app/chat.send
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;

    /**
     * 处理客户端通过 WebSocket 发送的消息
     * 客户端发送到 /app/chat.send
     */
    @MessageMapping("/chat.send")
    public Message sendMessage(@Payload ChatMessagePayload payload, Principal principal) {
        Long senderId = SecurityUtil.getCurrentUserId();
        log.info("WebSocket发送消息: senderId={}, conversationId={}", senderId, payload.getConversationId());

        MessageSendRequest request = new MessageSendRequest();
        request.setContent(payload.getContent());
        request.setType(payload.getType());

        return chatService.sendMessage(payload.getConversationId(), senderId, request);
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
