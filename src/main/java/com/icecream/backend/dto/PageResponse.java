package com.icecream.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 分页响应类，扩展ApiResponse，增加分页相关信息
 * @param <T> 数据泛型
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> extends ApiResponse<T> {
    private Long total;
    private Integer page;
    private Integer size;

    public PageResponse(boolean success, String message, T data, LocalDateTime timestamp, Long total, Integer page, Integer size) {
        super(success, message, data, timestamp);
        this.total = total;
        this.page = page;
        this.size = size;
    }

    /**
     * 创建成功分页响应
     * @param message 消息
     * @param data 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> success(String message, T data, Long total, Integer page, Integer size) {
        return PageResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .total(total)
                .page(page)
                .size(size)
                .build();
    }

    /**
     * 创建成功分页响应（使用默认成功消息）
     * @param data 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> success(T data, Long total, Integer page, Integer size) {
        return success("查询成功", data, total, page, size);
    }
}