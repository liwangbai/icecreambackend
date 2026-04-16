package com.icecream.backend.util;

import com.icecream.backend.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件处理工具类
 * 提供文件上传、验证、处理等相关工具方法
 */
@Slf4j
public class FileUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 验证文件是否安全
     * @param file 上传的文件
     * @param maxSize 最大文件大小
     * @param allowedTypes 允许的文件类型列表
     * @param allowedExtensions 允许的文件扩展名列表
     * @throws FileUploadException 如果文件验证失败
     */
    public static void validateFile(MultipartFile file, long maxSize,
                                   java.util.List<String> allowedTypes,
                                   java.util.List<String> allowedExtensions) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw FileUploadException.fileIsEmpty();
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw FileUploadException.fileSizeExceeded(maxSize);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        boolean typeAllowed = false;

        // 检查MIME类型
        if (contentType != null && allowedTypes.contains(contentType.toLowerCase())) {
            typeAllowed = true;
        }

        // 检查文件扩展名
        if (!typeAllowed && originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (allowedExtensions.contains(extension)) {
                typeAllowed = true;
            }
        }

        if (!typeAllowed) {
            throw FileUploadException.fileTypeNotSupported(
                    "MIME类型: " + String.join(", ", allowedTypes) +
                    " 或扩展名: " + String.join(", ", allowedExtensions)
            );
        }

        // 检查文件名安全性（防止路径遍历攻击）
        if (originalFilename != null) {
            validateFilename(originalFilename);
        }
    }

    /**
     * 验证文件名安全性，防止路径遍历攻击
     * @param filename 文件名
     * @throws FileUploadException 如果检测到路径遍历攻击
     */
    public static void validateFilename(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw FileUploadException.pathTraversalDetected(filename);
        }
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名（不含点）
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename) || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * 生成唯一文件名
     * @param originalFilename 原始文件名
     * @param prefix 文件名前缀（可选）
     * @return 唯一文件名
     */
    public static String generateUniqueFilename(String originalFilename, String prefix) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        StringBuilder filename = new StringBuilder();
        if (StringUtils.hasText(prefix)) {
            filename.append(prefix).append("_");
        }
        filename.append(uuid);

        if (StringUtils.hasText(extension)) {
            filename.append(".").append(extension);
        }

        return filename.toString();
    }

    /**
     * 生成按日期分层的目录路径
     * @param baseDir 基础目录
     * @param category 文件分类（如：avatars, posts等）
     * @param userId 用户ID（可选）
     * @return 完整的目录路径
     */
    public static String generateDateBasedPath(String baseDir, String category, Long userId) {
        String datePath = LocalDateTime.now().format(DATE_FORMATTER);

        StringBuilder path = new StringBuilder(baseDir);

        // 确保基础目录以分隔符结尾
        if (!baseDir.endsWith(File.separator)) {
            path.append(File.separator);
        }

        // 添加分类目录
        if (StringUtils.hasText(category)) {
            path.append(category).append(File.separator);
        }

        // 添加日期目录
        path.append(datePath).append(File.separator);

        // 添加用户目录（如果提供了用户ID）
        if (userId != null) {
            path.append(userId).append(File.separator);
        }

        return path.toString();
    }

    /**
     * 创建目录（如果不存在）
     * @param dirPath 目录路径
     * @return 创建的目录对象
     * @throws IOException 如果目录创建失败
     */
    public static File createDirectoryIfNotExists(String dirPath) throws IOException {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("无法创建目录: " + dirPath);
            }
            log.info("创建目录: {}", dirPath);
        }
        return directory;
    }

    /**
     * 保存文件到指定路径
     * @param file 上传的文件
     * @param targetPath 目标路径
     * @param overwrite 是否覆盖已存在的文件
     * @return 保存的文件对象
     * @throws IOException 如果文件保存失败
     */
    public static File saveFile(MultipartFile file, String targetPath, boolean overwrite) throws IOException {
        Path target = Paths.get(targetPath);

        // 如果文件已存在且不允许覆盖，则抛出异常
        if (Files.exists(target) && !overwrite) {
            throw new IOException("文件已存在: " + targetPath);
        }

        // 确保父目录存在
        Files.createDirectories(target.getParent());

        // 保存文件
        try (InputStream inputStream = file.getInputStream()) {
            if (overwrite) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(inputStream, target);
            }
        }

        log.info("文件保存成功: {}", targetPath);
        return target.toFile();
    }

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("文件删除成功: {}", filePath);
            } else {
                log.warn("文件不存在，无法删除: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 计算文件的MD5值
     * @param file 文件对象
     * @return MD5哈希值
     * @throws IOException 如果文件读取失败
     * @throws NoSuchAlgorithmException 如果MD5算法不可用
     */
    public static String calculateMd5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }
        byte[] digest = md.digest();

        // 转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 获取文件大小的人类可读格式
     * @param size 文件大小（字节）
     * @return 人类可读的文件大小字符串
     */
    public static String getHumanReadableSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        int exp = (int) (Math.log(size) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), unit);
    }

    /**
     * 清理文件名中的非法字符
     * @param filename 原始文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        // 移除路径分隔符和特殊字符
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 检查文件是否存在且可读
     * @param filePath 文件路径
     * @return 文件是否存在且可读
     */
    public static boolean isFileReadable(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile() && file.canRead();
    }

    /**
     * 获取文件的MIME类型
     * @param file 文件对象
     * @return MIME类型
     * @throws IOException 如果文件读取失败
     */
    public static String getMimeType(File file) throws IOException {
        return Files.probeContentType(file.toPath());
    }
}