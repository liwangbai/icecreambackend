package com.icecream.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门标签DTO
 * 用于返回热门自定义标签及其使用次数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotTagDTO {
    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 使用次数
     */
    private Integer count;
}
