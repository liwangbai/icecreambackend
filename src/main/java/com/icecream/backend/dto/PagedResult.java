package com.icecream.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResult<T> {
    private List<T> content;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;

    /**
     * 计算总页数
     * @return 总页数
     */
    public Integer getTotalPages() {
        if (total == null || size == null || size == 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    /**
     * 创建分页结果
     * @param content 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 分页结果对象
     */
    public static <T> PagedResult<T> of(List<T> content, Long total, Integer page, Integer size) {
        return PagedResult.<T>builder()
                .content(content)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }
}