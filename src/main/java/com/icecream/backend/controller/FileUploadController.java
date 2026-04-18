package com.icecream.backend.controller;

import com.icecream.backend.dto.ApiResponse;
import com.icecream.backend.dto.FileInfo;
import com.icecream.backend.dto.FileUploadResponse;
import com.icecream.backend.service.FileUploadService;
import com.icecream.backend.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传控制器
 * 提供文件上传、下载、删除等REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 上传单个文件
     * @param file 上传的文件
     * @param category 文件分类（如：avatars, posts等）
     * @param description 文件描述（可选）
     * @return 文件上传响应
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传单个文件", description = "上传单个文件到服务器")
    public ApiResponse<FileUploadResponse> uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "文件分类（如：avatars, posts等）", required = true)
            @RequestParam("category") String category,

            @Parameter(description = "文件描述（可选）")
            @RequestParam(value = "description", required = false) String description) {

        try {
            // 获取当前用户ID
            Long userId = SecurityUtil.getCurrentUserId();

            // 上传文件
            FileUploadResponse response = fileUploadService.uploadFile(file, category, userId, description);

            log.info("用户 {} 上传文件成功: {}, 分类: {}",
                    userId, file.getOriginalFilename(), category);

            return ApiResponse.success("文件上传成功", response);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return ApiResponse.<FileUploadResponse>error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     * @param file 头像文件
     * @return 文件上传响应
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传用户头像", description = "上传用户头像图片")
    public ApiResponse<FileUploadResponse> uploadAvatar(
            @Parameter(description = "头像文件", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            // 获取当前用户ID
            Long userId = SecurityUtil.getCurrentUserId();

            // 上传头像文件
            FileUploadResponse response = fileUploadService.uploadFile(file, "avatars", userId, "用户头像");

            log.info("用户 {} 上传头像成功: {}", userId, file.getOriginalFilename());

            return ApiResponse.success("头像上传成功", response);
        } catch (IOException e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            return ApiResponse.<FileUploadResponse>error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传帖子图片
     * @param file 帖子图片文件
     * @param description 图片描述（可选）
     * @return 文件上传响应
     */
    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传帖子图片", description = "上传帖子相关图片")
    public ApiResponse<FileUploadResponse> uploadPostImage(
            @Parameter(description = "帖子图片文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "图片描述（可选）")
            @RequestParam(value = "description", required = false) String description) {

        try {
            // 获取当前用户ID
            Long userId = SecurityUtil.getCurrentUserId();

            // 上传帖子图片
            FileUploadResponse response = fileUploadService.uploadFile(file, "posts", userId, description);

            log.info("用户 {} 上传帖子图片成功: {}", userId, file.getOriginalFilename());

            return ApiResponse.success("帖子图片上传成功", response);
        } catch (IOException e) {
            log.error("帖子图片上传失败: {}", e.getMessage(), e);
            return ApiResponse.<FileUploadResponse>error("帖子图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @return 操作结果
     */
    @DeleteMapping("/{category}/{filename}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除文件", description = "删除指定文件")
    public ApiResponse<Void> deleteFile(
            @Parameter(description = "文件名（存储的文件名）", required = true)
            @PathVariable("filename") String filename,

            @Parameter(description = "文件分类", required = true)
            @PathVariable("category") String category) {

        try {
            // 获取当前用户ID
            Long userId = SecurityUtil.getCurrentUserId();

            // 删除文件
            boolean deleted = fileUploadService.deleteFile(filename, category, userId);

            if (deleted) {
                log.info("用户 {} 删除文件成功: {}, 分类: {}", userId, filename, category);
                return ApiResponse.success("文件删除成功");
            } else {
                log.warn("用户 {} 删除文件失败: {}, 分类: {}", userId, filename, category);
                return ApiResponse.<Void>error("文件删除失败");
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return ApiResponse.<Void>error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @return 文件信息
     */
    @GetMapping("/{category}/{filename}/info")
    @Operation(summary = "获取文件信息", description = "获取指定文件的详细信息")
    public ApiResponse<FileInfo> getFileInfo(
            @Parameter(description = "文件名（存储的文件名）", required = true)
            @PathVariable("filename") String filename,

            @Parameter(description = "文件分类", required = true)
            @PathVariable("category") String category) {

        try {
            // 获取当前用户ID（如果已认证）
            Long userId = SecurityUtil.getCurrentUserIdOrNull();

            // 获取文件信息
            FileInfo fileInfo = fileUploadService.getFileInfo(filename, category, userId);

            log.debug("获取文件信息成功: {}, 分类: {}", filename, category);

            return ApiResponse.success(fileInfo);
        } catch (IOException e) {
            log.error("获取文件信息失败: {}", e.getMessage(), e);
            return ApiResponse.<FileInfo>error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @param response HTTP响应
     */
    @GetMapping("/{category}/{filename}")
    @Operation(summary = "下载文件", description = "下载指定文件")
    public void downloadFile(
            @Parameter(description = "文件名（存储的文件名）", required = true)
            @PathVariable("filename") String filename,

            @Parameter(description = "文件分类", required = true)
            @PathVariable("category") String category,

            HttpServletResponse response) {

        try {
            // 获取当前用户ID（如果已认证）
            Long userId = SecurityUtil.getCurrentUserIdOrNull();

            // 获取文件对象
            File file = fileUploadService.getFile(filename, category, userId);

            // 设置响应头
            String contentType = Files.probeContentType(file.toPath());
            if (!StringUtils.hasText(contentType)) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + filename + "\"");
            response.setContentLengthLong(file.length());

            // 将文件内容写入响应
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();

            log.debug("文件下载成功: {}, 分类: {}", filename, category);
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 获取文件作为Resource（用于Spring的Resource响应）
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @return 文件Resource
     */
    @GetMapping("/{category}/{filename}/resource")
    @Operation(summary = "获取文件Resource", description = "获取文件作为Spring Resource")
    public ResponseEntity<Resource> getFileAsResource(
            @Parameter(description = "文件名（存储的文件名）", required = true)
            @PathVariable("filename") String filename,

            @Parameter(description = "文件分类", required = true)
            @PathVariable("category") String category) {

        try {
            // 获取当前用户ID（如果已认证）
            Long userId = SecurityUtil.getCurrentUserIdOrNull();

            // 获取文件路径
            String filePath = fileUploadService.getFilePath(filename, category, userId);
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                // 获取文件MIME类型
                String contentType = Files.probeContentType(path);
                if (!StringUtils.hasText(contentType)) {
                    contentType = "application/octet-stream";
                }

                log.debug("获取文件Resource成功: {}, 分类: {}", filename, category);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                log.warn("文件不存在或不可读: {}, 分类: {}", filename, category);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("文件URL格式错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("获取文件Resource失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查文件是否存在
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @return 检查结果
     */
    @GetMapping("/{category}/{filename}/exists")
    @Operation(summary = "检查文件是否存在", description = "检查指定文件是否存在")
    public ApiResponse<Boolean> checkFileExists(
            @Parameter(description = "文件名（存储的文件名）", required = true)
            @PathVariable("filename") String filename,

            @Parameter(description = "文件分类", required = true)
            @PathVariable("category") String category) {

        try {
            // 获取当前用户ID（如果已认证）
            Long userId = SecurityUtil.getCurrentUserIdOrNull();

            // 检查文件是否存在
            boolean exists = fileUploadService.fileExists(filename, category, userId);

            log.debug("检查文件是否存在: {}, 分类: {}, 结果: {}", filename, category, exists);

            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("检查文件是否存在失败: {}", e.getMessage(), e);
            return ApiResponse.<Boolean>error("检查文件是否存在失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件（最多5个）
     * @param files 文件数组
     * @param category 文件分类
     * @return 批量上传结果
     */
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件（最多5个）")
    public ApiResponse<java.util.List<FileUploadResponse>> uploadFiles(
            @Parameter(description = "文件数组（最多5个）", required = true)
            @RequestParam("files") MultipartFile[] files,

            @Parameter(description = "文件分类", required = true)
            @RequestParam("category") String category) {

        // 限制最多上传5个文件
        if (files.length > 5) {
            return ApiResponse.<java.util.List<FileUploadResponse>>error("最多只能上传5个文件");
        }

        java.util.List<FileUploadResponse> responses = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 获取当前用户ID
                Long userId = SecurityUtil.getCurrentUserId();

                // 上传文件
                FileUploadResponse response = fileUploadService.uploadFile(file, category, userId, null);
                responses.add(response);

                log.info("用户 {} 批量上传文件成功: {}, 分类: {}",
                        userId, file.getOriginalFilename(), category);
            } catch (IOException e) {
                log.error("批量上传文件失败: {}", e.getMessage(), e);
                responses.add(FileUploadResponse.builder()
                        .originalFilename(file.getOriginalFilename())
                        .storedFilename("")
                        .fileUrl("")
                        .fileSize(0L)
                        .fileType(file.getContentType())
                        .uploadTime(java.time.LocalDateTime.now())
                        .category(category)
                        .description("上传失败: " + e.getMessage())
                        .build());
            }
        }

        return ApiResponse.success("批量上传完成", responses);
    }
}