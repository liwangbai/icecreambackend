package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.TagCreateRequest;
import com.icecream.backend.dto.request.TagUpdateRequest;
import com.icecream.backend.mapper.TagMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Tag;
import com.icecream.backend.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 标签服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final UserMapper userMapper;

    @Override
    public List<Tag> getAllTags() {
        log.debug("获取所有标签");
        return tagMapper.findAll();
    }

    @Override
    public List<Tag> getActiveTags() {
        log.debug("获取启用标签");
        return tagMapper.findActiveTags();
    }

    @Override
    public Tag getTagById(Long tagId) {
        log.debug("获取标签详情: tagId={}", tagId);
        Optional<Tag> tagOpt = tagMapper.findById(tagId);
        if (!tagOpt.isPresent()) {
            throw new RuntimeException("标签不存在: " + tagId);
        }
        return tagOpt.get();
    }

    @Override
    public Tag getTagByName(String name) {
        log.debug("根据名称获取标签: name={}", name);
        Optional<Tag> tagOpt = tagMapper.findByName(name);
        if (!tagOpt.isPresent()) {
            throw new RuntimeException("标签不存在: " + name);
        }
        return tagOpt.get();
    }

    @Override
    @Transactional
    public Tag createTag(TagCreateRequest request) {
        log.info("创建标签: name={}", request.getName());

        // 检查标签名称是否已存在
        Optional<Tag> existingTag = tagMapper.findByName(request.getName());
        if (existingTag.isPresent()) {
            throw new RuntimeException("标签名称已存在: " + request.getName());
        }

        // 创建标签对象
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        tag.setColor(request.getColor());
        tag.setIcon(request.getIcon());
        tag.setIsActive(request.getIsActive());
        tag.setUseCount(0);
        tag.setSortOrder(request.getSortOrder());
        tag.setCreatedBy(1L); // TODO: 从SecurityContext获取当前用户ID

        // 插入数据库
        tagMapper.insert(tag);

        log.info("标签创建成功: tagId={}, name={}", tag.getId(), tag.getName());
        return tag;
    }

    @Override
    @Transactional
    public Tag updateTag(Long tagId, TagUpdateRequest request) {
        log.info("更新标签: tagId={}", tagId);

        Optional<Tag> tagOpt = tagMapper.findById(tagId);
        if (!tagOpt.isPresent()) {
            throw new RuntimeException("标签不存在: " + tagId);
        }

        Tag tag = tagOpt.get();

        // 更新字段
        if (request.getName() != null && !request.getName().equals(tag.getName())) {
            // 检查新名称是否已被其他标签使用
            Optional<Tag> existingTag = tagMapper.findByName(request.getName());
            if (existingTag.isPresent() && !existingTag.get().getId().equals(tagId)) {
                throw new RuntimeException("标签名称已被其他标签使用: " + request.getName());
            }
            tag.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            tag.setIcon(request.getIcon());
        }
        if (request.getIsActive() != null) {
            tag.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            tag.setSortOrder(request.getSortOrder());
        }

        // 更新数据库
        tagMapper.update(tag);

        return getTagById(tagId);
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        log.info("删除标签: tagId={}", tagId);

        Optional<Tag> tagOpt = tagMapper.findById(tagId);
        if (!tagOpt.isPresent()) {
            throw new RuntimeException("标签不存在: " + tagId);
        }

        Tag tag = tagOpt.get();

        // 检查标签是否正在被使用
        if (tag.getUseCount() > 0) {
            throw new RuntimeException("标签正在被使用，无法删除");
        }

        // 删除标签
        tagMapper.delete(tagId);

        log.info("标签删除成功: tagId={}", tagId);
    }

    @Override
    public List<Tag> getPopularTags(int limit) {
        log.debug("获取热门标签: limit={}", limit);
        return tagMapper.findPopularTags(limit);
    }

    @Override
    public List<Tag> getTagsByPostId(Long postId) {
        log.debug("根据帖子ID获取标签: postId={}", postId);
        return tagMapper.findTagsByPostId(postId);
    }

    @Override
    @Transactional
    public void incrementUseCount(Long tagId) {
        log.debug("增加标签使用次数: tagId={}", tagId);
        tagMapper.incrementUseCount(tagId);
    }

    @Override
    @Transactional
    public void decrementUseCount(Long tagId) {
        log.debug("减少标签使用次数: tagId={}", tagId);
        tagMapper.decrementUseCount(tagId);
    }
}