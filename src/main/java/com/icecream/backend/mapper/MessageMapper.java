package com.icecream.backend.mapper;

import com.icecream.backend.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 消息数据访问接口
 */
@Mapper
public interface MessageMapper {

    /**
     * 根据ID查询消息
     */
    Optional<Message> findById(@Param("id") Long id);

    /**
     * 查询会话的消息列表（分页）
     */
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 统计会话的消息数
     */
    long countByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 统计会话中某用户未读消息数（大于lastReadMessageId的消息）
     */
    long countUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * 插入消息
     */
    int insert(Message message);

    /**
     * 撤回消息（软删除）
     */
    int softDelete(@Param("id") Long id);

    /**
     * 批量撤回会话下的所有消息（会话被删除时调用）
     */
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
