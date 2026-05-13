package com.icecream.backend.dto.request;

import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 帖子更新请求
 */
@Data
public class PostUpdateRequest {
    /**
     * 帖子标题
     */
    @Size(max = 200, message = "帖子标题长度不能超过200个字符")
    private String title;

    /**
     * 帖子内容
     */
    @Size(max = 500, message = "帖子内容长度不能超过500个字符")
    private String content;

    /**
     * 门派
     */
    @Size(max = 50, message = "门派长度不能超过50个字符")
    private String faction;

    /**
     * 大区
     */
    @Size(max = 50, message = "大区长度不能超过50个字符")
    private String region;

    /**
     * 服务器
     */
    @Size(max = 50, message = "服务器长度不能超过50个字符")
    private String server;

    /**
     * 体型
     */
    @Size(max = 20, message = "体型长度不能超过20个字符")
    private String bodyType;

    /**
     * 玩法
     */
    @Size(max = 50, message = "玩法长度不能超过50个字符")
    private String gameplay;

    /**
     * 寻找目标
     */
    @Size(max = 100, message = "寻找目标长度不能超过100个字符")
    private String target;

    /**
     * 联系方式
     */
    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    private String contactDetail;

    /**
     * 图片链接列表
     */
    private List<String> imageUrls;

    /**
     * 自定义标签列表，最多5个
     */
    @Size(max = 5, message = "标签最多5个")
    private List<@Size(max = 20, message = "单个标签长度不能超过20个字符") String> tags;
}
