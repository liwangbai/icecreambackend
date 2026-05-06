package com.icecream.backend.mapper;

import com.icecream.backend.model.MessageRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 消息已读状态数据访问接口
 */
@Mapper
public interface MessageReadMapper {

    /**
     * 查询用户在某会话中的已读状态
     */
    Optional<MessageRead> findByConversationAndUser(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    /**
     * 插入或更新已读状态
     */
    int insertOrUpdate(MessageRead messageRead);

    /**
     * 更新已读消息ID
     */
    int updateLastRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("lastReadMessageId") Long lastReadMessageId);
}
