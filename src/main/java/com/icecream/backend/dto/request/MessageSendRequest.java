package com.icecream.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送消息请求
 */
@Data
public class MessageSendRequest {
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容不能超过5000个字符")
    private String content;

    /**
     * 消息类型：1-文本（默认），2-图片
     */
    private Integer type = 1;
}
