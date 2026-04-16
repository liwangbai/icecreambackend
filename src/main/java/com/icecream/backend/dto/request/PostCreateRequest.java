package com.icecream.backend.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 帖子创建请求
 */
@Data
public class PostCreateRequest {
    /**
     * 帖子标题
     */
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 200, message = "帖子标题长度不能超过200个字符")
    private String title;

    /**
     * 帖子内容
     */
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 10000, message = "帖子内容长度不能超过10000个字符")
    private String content;

    /**
     * 帖子摘要
     */
    @Size(max = 500, message = "帖子摘要长度不能超过500个字符")
    private String summary;

    /**
     * 封面图片URL
     */
    private String coverImageUrl;

    /**
     * 帖子状态：0-草稿，1-已发布
     */
    private Integer status = 1;

    /**
     * 可见性：0-私密，1-公开
     */
    private Integer visibility = 1;

    /**
     * 是否置顶
     */
    private Boolean isTop = false;

    /**
     * 标签ID列表，帖子必须包含至少一个标签
     */
    @NotEmpty(message = "帖子必须包含至少一个标签")
    private List<Long> tagIds;
}