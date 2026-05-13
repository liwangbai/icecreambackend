package com.icecream.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件上传响应DTO
 * 用于返回文件上传成功后的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 存储的文件名（唯一标识）
     */
    private String storedFilename;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件存储路径
     */
    private String storagePath;

    /**
     * 文件上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 文件分类（如：avatars, posts等）
     */
    private String category;

    /**
     * 文件描述（可选）
     */
    private String description;

    /**
     * 文件MD5校验码（可选）
     */
    private String md5;
}