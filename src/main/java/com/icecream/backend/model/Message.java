package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体类
 * 对应数据库中的messages表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {
    /**
     * 所属会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-文本，2-图片，3-系统消息
     */
    private Integer type;

    /**
     * 状态：1-正常，0-已撤回
     */
    private Integer status;

    /**
     * 发送者信息（查询时填充）
     */
    private User sender;
}
