package com.icecream.backend.service;

import com.icecream.backend.dto.request.TagCreateRequest;
import com.icecream.backend.dto.request.TagUpdateRequest;
import com.icecream.backend.model.Tag;

import java.util.List;

/**
 * 标签服务接口
 * 负责预定义标签的管理和查询
 * 注意：标签管理功能通常仅管理员使用
 */
public interface TagService {

    /**
     * 获取所有标签（管理员使用）
     * @return 标签列表
     */
    List<Tag> getAllTags();

    /**
     * 获取所有启用的标签
     * @return 启用的标签列表
     */
    List<Tag> getActiveTags();

    /**
     * 根据ID获取标签
     * @param tagId 标签ID
     * @return 标签信息
     */
    Tag getTagById(Long tagId);

    /**
     * 根据名称获取标签
     * @param name 标签名称
     * @return 标签信息
     */
    Tag getTagByName(String name);

    /**
     * 创建新标签（仅管理员）
     * @param request 创建请求
     * @return 创建的标签
     */
    Tag createTag(TagCreateRequest request);

    /**
     * 更新标签信息（仅管理员）
     * @param tagId 标签ID
     * @param request 更新请求
     * @return 更新后的标签
     */
    Tag updateTag(Long tagId, TagUpdateRequest request);

    /**
     * 删除标签（仅管理员）
     * 注意：如果标签正在被使用，可能需要特殊处理
     * @param tagId 标签ID
     */
    void deleteTag(Long tagId);

    /**
     * 获取热门标签
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    List<Tag> getPopularTags(int limit);

    /**
     * 根据帖子ID获取标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<Tag> getTagsByPostId(Long postId);

    /**
     * 增加标签使用次数
     * @param tagId 标签ID
     */
    void incrementUseCount(Long tagId);

    /**
     * 减少标签使用次数
     * @param tagId 标签ID
     */
    void decrementUseCount(Long tagId);
}