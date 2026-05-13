package com.icecream.backend.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 基础实体类，所有实体类都应继承此类
 * 包含通用的ID和时间戳字段
 */
@Data
public abstract class BaseEntity {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}