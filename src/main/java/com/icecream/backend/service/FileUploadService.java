package com.icecream.backend.service;

import com.icecream.backend.dto.FileInfo;
import com.icecream.backend.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件上传服务接口
 * 定义文件上传、下载、删除等操作的标准接口
 */
public interface FileUploadService {

    /**
     * 上传单个文件
     * @param file 上传的文件
     * @param category 文件分类（如：avatars, posts等）
     * @param userId 用户ID（用于按用户分目录）
     * @param description 文件描述（可选）
     * @return 文件上传响应信息
     * @throws IOException 如果文件上传失败
     */
    FileUploadResponse uploadFile(MultipartFile file, String category, Long userId, String description) throws IOException;

    /**
     * 上传单个文件（简化版）
     * @param file 上传的文件
     * @param category 文件分类
     * @param userId 用户ID
     * @return 文件上传响应信息
     * @throws IOException 如果文件上传失败
     */
    FileUploadResponse uploadFile(MultipartFile file, String category, Long userId) throws IOException;

    /**
     * 上传单个文件（最简版）
     * @param file 上传的文件
     * @param category 文件分类
     * @return 文件上传响应信息
     * @throws IOException 如果文件上传失败
     */
    FileUploadResponse uploadFile(MultipartFile file, String category) throws IOException;

    /**
     * 删除文件
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @param userId 用户ID（用于验证文件所有权）
     * @return 是否删除成功
     */
    boolean deleteFile(String filename, String category, Long userId);

    /**
     * 获取文件信息
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @param userId 用户ID（用于验证文件访问权限）
     * @return 文件信息
     * @throws IOException 如果文件不存在或无法访问
     */
    FileInfo getFileInfo(String filename, String category, Long userId) throws IOException;

    /**
     * 获取文件对象
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @param userId 用户ID（用于验证文件访问权限）
     * @return 文件对象
     * @throws IOException 如果文件不存在或无法访问
     */
    File getFile(String filename, String category, Long userId) throws IOException;

    /**
     * 获取文件访问URL
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @param userId 用户ID
     * @return 文件访问URL
     */
    String getFileUrl(String filename, String category, Long userId);

    /**
     * 验证文件是否存在
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @param userId 用户ID
     * @return 文件是否存在
     */
    boolean fileExists(String filename, String category, Long userId);

    /**
     * 获取文件存储路径
     * @param filename 文件名（存储的文件名）
     * @param category 文件分类
     * @param userId 用户ID
     * @return 文件存储路径
     */
    String getFilePath(String filename, String category, Long userId);

    /**
     * 清理临时文件
     * 清理超过指定时间的临时文件
     * @param maxAgeHours 最大保留时间（小时）
     * @return 清理的文件数量
     */
    int cleanupTempFiles(int maxAgeHours);

    /**
     * 获取存储服务类型
     * @return 存储服务类型（如：local, oss等）
     */
    String getStorageType();
}