package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息已读状态实体类
 * 对应数据库中的message_reads表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageRead extends BaseEntity {
    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 最后已读消息ID
     */
    private Long lastReadMessageId;

    /**
     * 最后已读时间
     */
    private java.time.LocalDateTime lastReadAt;
}
