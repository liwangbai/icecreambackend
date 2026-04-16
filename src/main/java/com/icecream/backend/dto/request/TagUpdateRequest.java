package com.icecream.backend.dto.request;

import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 标签更新请求（仅管理员使用）
 */
@Data
public class TagUpdateRequest {
    /**
     * 标签名称，必须唯一
     */
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;

    /**
     * 标签描述
     */
    @Size(max = 200, message = "标签描述长度不能超过200个字符")
    private String description;

    /**
     * 标签颜色（十六进制颜色代码）
     */
    @Size(max = 20, message = "颜色代码长度不能超过20个字符")
    private String color;

    /**
     * 标签图标（图标类名或URL）
     */
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 排序顺序（数字越小越靠前）
     */
    private Integer sortOrder;
}