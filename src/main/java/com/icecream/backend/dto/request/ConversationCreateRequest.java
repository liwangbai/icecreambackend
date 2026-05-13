package com.icecream.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建会话请求
 */
@Data
public class ConversationCreateRequest {
    /**
     * 对方用户ID
     */
    @NotNull(message = "对方用户ID不能为空")
    private Long otherUserId;
}
