package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.request.TagCreateRequest;
import com.icecream.backend.dto.request.TagUpdateRequest;
import com.icecream.backend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 标签控制器
 * 处理预定义标签的管理和查询
 * 注意：大部分标签管理功能需要管理员权限
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "预定义标签的管理和查询接口")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "获取所有标签", description = "获取所有标签（包括未启用的），通常仅管理员使用")
    public ResponseEntity<ApiResponse<List<com.icecream.backend.model.Tag>>> getAllTags() {
        log.debug("获取所有标签");
        List<com.icecream.backend.model.Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success("获取成功", tags));
    }

    @GetMapping("/active")
    @Operation(summary = "获取启用标签", description = "获取所有启用的标签，普通用户使用")
    public ResponseEntity<ApiResponse<List<com.icecream.backend.model.Tag>>> getActiveTags() {
        log.debug("获取启用标签");
        List<com.icecream.backend.model.Tag> tags = tagService.getActiveTags();
        return ResponseEntity.ok(ApiResponse.success("获取成功", tags));
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门标签", description = "获取热门标签（按使用次数降序）")
    public ResponseEntity<ApiResponse<List<com.icecream.backend.model.Tag>>> getPopularTags(
            @io.swagger.v3.oas.annotations.Parameter(description = "返回数量限制")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.debug("获取热门标签: limit={}", limit);
        List<com.icecream.backend.model.Tag> tags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(ApiResponse.success("获取成功", tags));
    }

    @GetMapping("/{tagId}")
    @Operation(summary = "获取标签详情", description = "根据ID获取标签详情")
    public ResponseEntity<ApiResponse<com.icecream.backend.model.Tag>> getTagById(@PathVariable Long tagId) {
        log.debug("获取标签详情: tagId={}", tagId);
        com.icecream.backend.model.Tag tag = tagService.getTagById(tagId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", tag));
    }

    @PostMapping
    @Operation(summary = "创建标签", description = "创建新标签（需要管理员权限）")
    public ResponseEntity<ApiResponse<com.icecream.backend.model.Tag>> createTag(@Valid @RequestBody TagCreateRequest request) {
        log.info("创建标签: name={}", request.getName());
        com.icecream.backend.model.Tag tag = tagService.createTag(request);
        return ResponseEntity.ok(ApiResponse.success("标签创建成功", tag));
    }

    @PutMapping("/{tagId}")
    @Operation(summary = "更新标签", description = "更新标签信息（需要管理员权限）")
    public ResponseEntity<ApiResponse<com.icecream.backend.model.Tag>> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateRequest request) {
        log.info("更新标签: tagId={}", tagId);
        com.icecream.backend.model.Tag tag = tagService.updateTag(tagId, request);
        return ResponseEntity.ok(ApiResponse.success("标签更新成功", tag));
    }

    @DeleteMapping("/{tagId}")
    @Operation(summary = "删除标签", description = "删除标签（需要管理员权限）")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        log.info("删除标签: tagId={}", tagId);
        tagService.deleteTag(tagId);
        return ResponseEntity.ok(ApiResponse.success("标签删除成功"));
    }

    @GetMapping("/{tagId}/posts")
    @Operation(summary = "获取标签下的帖子", description = "获取包含指定标签的所有帖子")
    public ResponseEntity<ApiResponse<List<com.icecream.backend.model.Post>>> getPostsByTagId(@PathVariable Long tagId) {
        log.debug("获取标签下的帖子: tagId={}", tagId);
        // 这里需要调用PostService的方法
        // 为了简单起见，暂时返回空列表
        // 实际应该调用: postService.getPostsByTagId(tagId)
        return ResponseEntity.ok(ApiResponse.success("获取成功", List.of()));
    }
}