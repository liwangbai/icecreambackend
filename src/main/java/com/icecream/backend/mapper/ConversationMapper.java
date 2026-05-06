package com.icecream.backend.mapper;

import com.icecream.backend.model.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 会话数据访问接口
 */
@Mapper
public interface ConversationMapper {

    /**
     * 根据ID查询会话
     */
    Optional<Conversation> findById(@Param("id") Long id);

    /**
     * 根据两个用户ID查询会话
     */
    Optional<Conversation> findByUserIds(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * 查询用户的所有会话（按最后消息时间倒序）
     */
    List<Conversation> findByUserId(@Param("userId") Long userId);

    /**
     * 插入会话
     */
    int insert(Conversation conversation);

    /**
     * 更新会话的最后消息
     */
    int updateLastMessage(@Param("id") Long id, @Param("lastMessageId") Long lastMessageId, @Param("lastMessageAt") java.time.LocalDateTime lastMessageAt);

    /**
     * 统计用户的会话数
     */
    long countByUserId(@Param("userId") Long userId);
}
