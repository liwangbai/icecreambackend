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
}
