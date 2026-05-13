package com.icecream.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传配置属性类
 * 用于管理文件上传相关的配置参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    /**
     * 文件上传基础目录
     * 默认值：./uploads
     */
    private String baseDir = "./uploads";

    /**
     * 允许的最大文件大小（字节）
     * 默认值：10MB = 10 * 1024 * 1024
     */
    private long maxFileSize = 10 * 1024 * 1024;

    /**
     * 允许的文件类型（MIME类型或扩展名）
     * 默认支持常见的图片格式
     */
    private List<String> allowedFileTypes = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
            "image/bmp", "image/svg+xml"
    );

    /**
     * 允许的文件扩展名
     * 默认支持常见的图片扩展名
     */
    private List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"
    );

    /**
     * 是否启用文件类型验证
     * 默认值：true
     */
    private boolean enableFileTypeValidation = true;

    /**
     * 是否启用文件大小验证
     * 默认值：true
     */
    private boolean enableFileSizeValidation = true;

    /**
     * 是否生成唯一文件名
     * 默认值：true
     */
    private boolean generateUniqueFilename = true;

    /**
     * 文件名前缀（可选）
     */
    private String filenamePrefix = "";

    /**
     * 文件访问URL前缀
     * 默认值：/uploads
     */
    private String urlPrefix = "/uploads";

    /**
     * 是否按用户分目录存储
     * 默认值：true
     */
    private boolean organizeByUser = true;

    /**
     * 是否按文件类型分目录存储
     * 默认值：true
     */
    private boolean organizeByType = true;

    /**
     * 用户头像存储目录名
     * 默认值：avatars
     */
    private String avatarDir = "avatars";

    /**
     * 帖子图片存储目录名
     * 默认值：posts
     */
    private String postDir = "posts";

    /**
     * 其他文件存储目录名
     * 默认值：others
     */
    private String otherDir = "others";

    /**
     * 是否启用文件MD5校验
     * 默认值：false
     */
    private boolean enableMd5Check = false;

    /**
     * 是否覆盖同名文件
     * 默认值：false
     */
    private boolean overwriteExisting = false;

    /**
     * 文件存储模式
     * 可选值：local（本地存储），未来可扩展为oss（对象存储）
     */
    private String storageMode = "local";

    /**
     * 获取完整的文件上传目录
     * @return 文件上传目录路径
     */
    public String getUploadDir() {
        return baseDir;
    }

    /**
     * 获取允许的文件类型字符串（用于错误信息）
     * @return 允许的文件类型字符串
     */
    public String getAllowedFileTypesString() {
        return String.join(", ", allowedFileTypes);
    }

    /**
     * 获取允许的文件扩展名字符串（用于错误信息）
     * @return 允许的文件扩展名字符串
     */
    public String getAllowedExtensionsString() {
        return String.join(", ", allowedExtensions);
    }

    /**
     * 检查文件类型是否被允许
     * @param contentType 文件MIME类型
     * @param filename 文件名
     * @return 是否允许
     */
    public boolean isFileTypeAllowed(String contentType, String filename) {
        if (!enableFileTypeValidation) {
            return true;
        }

        // 检查MIME类型
        if (contentType != null && allowedFileTypes.contains(contentType.toLowerCase())) {
            return true;
        }

        // 检查文件扩展名
        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            return allowedExtensions.contains(extension);
        }

        return false;
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}