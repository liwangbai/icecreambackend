package com.icecream.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新评论请求
 */
@Data
@Schema(description = "更新评论请求")
public class CommentUpdateRequest {

    @Schema(description = "评论内容，最多500字符")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字符")
    private String content;
}