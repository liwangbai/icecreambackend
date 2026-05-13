package com.icecream.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户浏览历史数据访问接口
 */
@Mapper
public interface UserBrowsingHistoryMapper {

    /**
     * 插入或更新浏览记录（同一用户对同一帖子只保留最新浏览时间）
     */
    int insertOrUpdate(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 清除用户全部浏览历史
     */
    int deleteAllByUserId(@Param("userId") Long userId);

    /**
     * 删除用户最旧的浏览记录，保留最近N条
     */
    int deleteOldestByUserId(@Param("userId") Long userId, @Param("keepCount") int keepCount);
}
