package com.icecream.backend.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * 帖子创建请求
 */
@Data
public class PostCreateRequest {
    /**
     * 帖子标题（选填）
     */
    @Size(max = 200, message = "帖子标题长度不能超过200个字符")
    private String title;

    /**
     * 帖子正文内容（必填）
     */
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 500, message = "帖子内容长度不能超过500个字符")
    private String content;

    /**
     * 门派（必填），例：纯阳
     */
    @NotBlank(message = "门派不能为空")
    @Size(max = 50, message = "门派长度不能超过50个字符")
    private String faction;

    /**
     * 大区（必填），例：电信区
     */
    @NotBlank(message = "大区不能为空")
    @Size(max = 50, message = "大区长度不能超过50个字符")
    private String region;

    /**
     * 服务器（必填），例：双梦
     */
    @NotBlank(message = "服务器不能为空")
    @Size(max = 50, message = "服务器长度不能超过50个字符")
    private String server;

    /**
     * 体型（必填），例：成男
     */
    @NotBlank(message = "体型不能为空")
    @Size(max = 20, message = "体型长度不能超过20个字符")
    private String bodyType;

    /**
     * 玩法（必填），例：PVP
     */
    @NotBlank(message = "玩法不能为空")
    @Size(max = 50, message = "玩法长度不能超过50个字符")
    private String gameplay;

    /**
     * 寻找目标（选填），例：队友
     */
    @Size(max = 100, message = "寻找目标长度不能超过100个字符")
    private String target;

    /**
     * 联系方式具体内容（选填），若选择"无"，传空字符串""
     */
    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    private String contactDetail;

    /**
     * 自定义标签列表（选填），最多5个，如 ["深圳", "招募"]
     */
    @Size(max = 5, message = "标签最多5个")
    private List<@Size(max = 20, message = "单个标签长度不能超过20个字符") String> tags;

    /**
     * 图片链接列表（选填），若没有图片，传空列表[]或不传
     */
    private List<String> imageUrls;
}