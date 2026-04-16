package com.icecream.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息DTO
 * 用于返回文件的基本信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    /**
     * 文件名
     */
    private String filename;

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
     * 最后修改时间
     */
    private LocalDateTime lastModified;

    /**
     * 文件创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 文件是否可读
     */
    private boolean readable;

    /**
     * 文件是否可写
     */
    private boolean writable;

    /**
     * 文件是否可执行
     */
    private boolean executable;

    /**
     * 文件分类（如：avatars, posts等）
     */
    private String category;

    /**
     * 文件所有者ID（用户ID）
     */
    private Long ownerId;

    /**
     * 文件描述（可选）
     */
    private String description;
}