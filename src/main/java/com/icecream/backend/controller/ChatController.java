package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.PagedResult;
import com.icecream.backend.dto.request.ConversationCreateRequest;
import com.icecream.backend.dto.request.MessageSendRequest;
import com.icecream.backend.model.Conversation;
import com.icecream.backend.model.Message;
import com.icecream.backend.service.ChatService;
import com.icecream.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "聊天管理", description = "私聊相关接口，包括会话管理、消息收发等")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    @Operation(summary = "创建或获取会话", description = "与指定用户创建会话，如已存在则返回现有会话")
    public ResponseEntity<ApiResponse<Conversation>> createConversation(
            @Valid @RequestBody ConversationCreateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("创建会话: userId={}, otherUserId={}", currentUserId, request.getOtherUserId());
        Conversation conversation = chatService.getOrCreateConversation(currentUserId, request.getOtherUserId());
        return ResponseEntity.ok(ApiResponse.success("会话创建成功", conversation));
    }

    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话")
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取会话列表: userId={}", currentUserId);
        List<Conversation> conversations = chatService.getConversations(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", conversations));
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "获取会话详情", description = "根据ID获取会话详情")
    public ResponseEntity<ApiResponse<Conversation>> getConversationById(@PathVariable Long conversationId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取会话详情: conversationId={}, userId={}", conversationId, currentUserId);
        Conversation conversation = chatService.getConversationById(conversationId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", conversation));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "获取会话消息", description = "获取会话的消息列表（分页）")
    public ResponseEntity<ApiResponse<PagedResult<Message>>> getMessages(
            @PathVariable Long conversationId,
            @Parameter(description = "页码，从0开始") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取消息列表: conversationId={}, page={}, size={}", conversationId, page, size);
        List<Message> messages = chatService.getMessages(conversationId, currentUserId, page, size);
        long total = chatService.countMessages(conversationId);
        PagedResult<Message> pagedResult = PagedResult.of(messages, total, page, size);
        return ResponseEntity.ok(ApiResponse.success("获取成功", pagedResult));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "发送消息", description = "在指定会话中发送消息")
    public ResponseEntity<ApiResponse<Message>> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageSendRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("发送消息: conversationId={}, userId={}", conversationId, currentUserId);
        Message message = chatService.sendMessage(conversationId, currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("消息发送成功", message));
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息给用户", description = "直接向某用户发送消息，自动创建会话")
    public ResponseEntity<ApiResponse<Message>> sendMessageToUser(
            @RequestParam Long receiverId,
            @Valid @RequestBody MessageSendRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("发送消息给用户: senderId={}, receiverId={}", currentUserId, receiverId);
        Message message = chatService.sendMessageToUser(currentUserId, receiverId, request);
        return ResponseEntity.ok(ApiResponse.success("消息发送成功", message));
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "撤回消息", description = "撤回消息（2分钟内有效）")
    public ResponseEntity<ApiResponse<Void>> recallMessage(@PathVariable Long messageId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("撤回消息: messageId={}, userId={}", messageId, currentUserId);
        chatService.recallMessage(messageId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("消息已撤回"));
    }

    @PutMapping("/conversations/{conversationId}/read")
    @Operation(summary = "标记已读", description = "标记会话中的所有消息为已读")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long conversationId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.info("标记已读: conversationId={}, userId={}", conversationId, currentUserId);
        chatService.markAsRead(conversationId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("已标记为已读"));
    }

    @GetMapping("/conversations/{conversationId}/unread")
    @Operation(summary = "获取未读数", description = "获取会话的未读消息数")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long conversationId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取未读数: conversationId={}, userId={}", conversationId, currentUserId);
        long count = chatService.getUnreadCount(conversationId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", count));
    }

    @GetMapping("/unread")
    @Operation(summary = "获取总未读数", description = "获取当前用户的所有未读消息总数")
    public ResponseEntity<ApiResponse<Long>> getTotalUnreadCount() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        log.debug("获取总未读数: userId={}", currentUserId);
        long count = chatService.getTotalUnreadCount(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", count));
    }
}
