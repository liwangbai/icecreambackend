package com.icecream.backend.service.impl;

import com.icecream.backend.config.FileUploadProperties;
import com.icecream.backend.dto.FileInfo;
import com.icecream.backend.dto.FileUploadResponse;
import com.icecream.backend.exception.FileUploadException;
import com.icecream.backend.service.FileUploadService;
import com.icecream.backend.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 本地文件上传服务实现类
 * 使用本地文件系统存储上传的文件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileUploadServiceImpl implements FileUploadService {

    private final FileUploadProperties fileUploadProperties;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String category, Long userId, String description) throws IOException {
        log.info("开始上传文件: {}, 分类: {}, 用户ID: {}",
                file.getOriginalFilename(), category, userId);

        // 验证文件
        FileUtil.validateFile(file,
                fileUploadProperties.getMaxFileSize(),
                fileUploadProperties.getAllowedFileTypes(),
                fileUploadProperties.getAllowedExtensions());

        // 生成存储路径
        String storagePath = generateStoragePath(category, userId);
        FileUtil.createDirectoryIfNotExists(storagePath);

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String storedFilename;
        if (fileUploadProperties.isGenerateUniqueFilename()) {
            storedFilename = FileUtil.generateUniqueFilename(originalFilename,
                    fileUploadProperties.getFilenamePrefix());
        } else {
            storedFilename = FileUtil.sanitizeFilename(originalFilename);
        }

        // 构建完整文件路径
        String filePath = storagePath + File.separator + storedFilename;

        // 保存文件
        File savedFile = FileUtil.saveFile(file, filePath,
                fileUploadProperties.isOverwriteExisting());

        // 计算MD5（如果启用）
        String md5 = null;
        if (fileUploadProperties.isEnableMd5Check()) {
            try {
                md5 = FileUtil.calculateMd5(savedFile);
                log.debug("文件MD5计算完成: {}", md5);
            } catch (NoSuchAlgorithmException e) {
                log.warn("MD5算法不可用，跳过MD5计算", e);
            }
        }

        // 生成文件访问URL
        String fileUrl = generateFileUrl(storedFilename, category, userId);

        // 构建响应
        FileUploadResponse response = FileUploadResponse.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .storagePath(filePath)
                .uploadTime(LocalDateTime.now())
                .category(category)
                .description(description)
                .md5(md5)
                .build();

        log.info("文件上传成功: {}, 存储路径: {}", storedFilename, filePath);
        return response;
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String category, Long userId) throws IOException {
        return uploadFile(file, category, userId, null);
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String category) throws IOException {
        return uploadFile(file, category, null, null);
    }

    @Override
    public boolean deleteFile(String filename, String category, Long userId) {
        log.info("开始删除文件: {}, 分类: {}, 用户ID: {}", filename, category, userId);

        // 验证文件名安全性
        FileUtil.validateFilename(filename);

        // 获取文件路径
        String filePath = getFilePath(filename, category, userId);

        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("文件不存在，无法删除: {}", filePath);
            return false;
        }

        // 验证文件所有权（如果提供了用户ID）
        if (userId != null) {
            // 这里可以添加更复杂的权限验证逻辑
            // 例如：检查文件路径是否包含用户ID目录
            if (!filePath.contains(File.separator + userId + File.separator) &&
                !filePath.contains(File.separator + userId)) {
                log.warn("用户 {} 无权删除文件: {}", userId, filePath);
                return false;
            }
        }

        // 删除文件
        boolean deleted = FileUtil.deleteFile(filePath);
        if (deleted) {
            log.info("文件删除成功: {}", filePath);
        }
        return deleted;
    }

    @Override
    public FileInfo getFileInfo(String filename, String category, Long userId) throws IOException {
        log.debug("获取文件信息: {}, 分类: {}, 用户ID: {}", filename, category, userId);

        // 验证文件名安全性
        FileUtil.validateFilename(filename);

        // 获取文件路径
        String filePath = getFilePath(filename, category, userId);
        File file = new File(filePath);

        // 检查文件是否存在
        if (!file.exists()) {
            throw FileUploadException.fileNotFound(filename);
        }

        // 检查文件访问权限
        if (!file.canRead()) {
            throw new IOException("文件不可读: " + filename);
        }

        // 获取文件属性
        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

        // 构建文件信息
        return FileInfo.builder()
                .filename(filename)
                .fileUrl(getFileUrl(filename, category, userId))
                .fileSize(file.length())
                .fileType(Files.probeContentType(file.toPath()))
                .lastModified(LocalDateTime.ofInstant(
                        attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()))
                .createdTime(LocalDateTime.ofInstant(
                        attrs.creationTime().toInstant(), ZoneId.systemDefault()))
                .readable(file.canRead())
                .writable(file.canWrite())
                .executable(file.canExecute())
                .category(category)
                .ownerId(userId)
                .build();
    }

    @Override
    public File getFile(String filename, String category, Long userId) throws IOException {
        log.debug("获取文件对象: {}, 分类: {}, 用户ID: {}", filename, category, userId);

        // 验证文件名安全性
        FileUtil.validateFilename(filename);

        // 获取文件路径
        String filePath = getFilePath(filename, category, userId);
        File file = new File(filePath);

        // 检查文件是否存在
        if (!file.exists()) {
            throw FileUploadException.fileNotFound(filename);
        }

        // 检查文件访问权限
        if (!file.canRead()) {
            throw new IOException("文件不可读: " + filename);
        }

        return file;
    }

    @Override
    public String getFileUrl(String filename, String category, Long userId) {
        // 构建文件访问URL
        StringBuilder urlBuilder = new StringBuilder(fileUploadProperties.getUrlPrefix());

        // 添加分类目录
        if (StringUtils.hasText(category)) {
            urlBuilder.append("/").append(category);
        }

        // 添加用户目录（如果提供了用户ID且启用了按用户分目录）
        if (userId != null && fileUploadProperties.isOrganizeByUser()) {
            urlBuilder.append("/").append(userId);
        }

        // 添加文件名
        urlBuilder.append("/").append(filename);

        return urlBuilder.toString();
    }

    @Override
    public boolean fileExists(String filename, String category, Long userId) {
        try {
            // 验证文件名安全性
            FileUtil.validateFilename(filename);

            String filePath = getFilePath(filename, category, userId);
            File file = new File(filePath);
            return file.exists() && file.isFile();
        } catch (Exception e) {
            log.warn("检查文件是否存在时发生错误: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFilePath(String filename, String category, Long userId) {
        // 生成存储路径
        String storagePath = generateStoragePath(category, userId);

        // 构建完整文件路径
        return storagePath + File.separator + filename;
    }

    @Override
    public int cleanupTempFiles(int maxAgeHours) {
        log.info("开始清理临时文件，最大保留时间: {}小时", maxAgeHours);

        // 这里可以实现临时文件清理逻辑
        // 例如：清理uploads/temp目录下超过指定时间的文件

        // 暂时返回0，表示没有清理任何文件
        // 实际项目中可以根据需要实现此功能
        return 0;
    }

    @Override
    public String getStorageType() {
        return "local";
    }

    /**
     * 生成文件存储路径
     * @param category 文件分类
     * @param userId 用户ID
     * @return 存储路径
     */
    private String generateStoragePath(String category, Long userId) {
        String baseDir = fileUploadProperties.getUploadDir();

        // 使用日期分层目录结构
        String path = FileUtil.generateDateBasedPath(baseDir, category,
                fileUploadProperties.isOrganizeByUser() ? userId : null);

        log.debug("生成存储路径: {}", path);
        return path;
    }

    /**
     * 获取分类目录名
     * @param category 分类标识
     * @return 目录名
     */
    private String getCategoryDir(String category) {
        if (!StringUtils.hasText(category)) {
            return fileUploadProperties.getOtherDir();
        }

        switch (category.toLowerCase()) {
            case "avatar":
            case "avatars":
                return fileUploadProperties.getAvatarDir();
            case "post":
            case "posts":
                return fileUploadProperties.getPostDir();
            default:
                return category;
        }
    }
}