package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签实体类
 * 对应数据库中的tags表（预定义标签）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Tag extends BaseEntity {
    /**
     * 标签名称，唯一
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签颜色（十六进制颜色代码）
     */
    private String color;

    /**
     * 标签图标（图标类名或URL）
     */
    private String icon;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 排序顺序（数字越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 创建人ID（管理员）
     */
    private Long createdBy;
}