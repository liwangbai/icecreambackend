package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话实体类
 * 对应数据库中的conversations表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Conversation extends BaseEntity {
    /**
     * 用户1ID（较小者，用于唯一标识会话）
     */
    private Long user1Id;

    /**
     * 用户2ID（较大者，用于唯一标识会话）
     */
    private Long user2Id;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息时间
     */
    private java.time.LocalDateTime lastMessageAt;

    /**
     * 最后一条消息（查询时填充）
     */
    private Message lastMessage;

    /**
     * 对方用户信息（查询时填充，当前用户看到的对方信息）
     */
    private User otherUser;

    /**
     * 当前用户的未读消息数（查询时填充）
     */
    private Integer unreadCount;
}
