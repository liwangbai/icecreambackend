package com.icecream.backend.service;

import com.icecream.backend.config.FileUploadProperties;
import com.icecream.backend.dto.FileUploadResponse;
import com.icecream.backend.exception.FileUploadException;
import com.icecream.backend.service.impl.LocalFileUploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 文件上传服务测试类
 */
class FileUploadServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileUploadServiceImpl fileUploadService;
    private FileUploadProperties fileUploadProperties;

    @BeforeEach
    void setUp() {
        fileUploadProperties = new FileUploadProperties();
        fileUploadProperties.setBaseDir(tempDir.toString());
        fileUploadProperties.setMaxFileSize(1024 * 1024); // 1MB
        fileUploadProperties.setEnableFileTypeValidation(true);
        fileUploadProperties.setEnableFileSizeValidation(true);
        fileUploadProperties.setGenerateUniqueFilename(true);
        fileUploadProperties.setUrlPrefix("/uploads");
        fileUploadProperties.setOrganizeByUser(true);
        fileUploadProperties.setOrganizeByType(true);

        fileUploadService = new LocalFileUploadServiceImpl(fileUploadProperties);
    }

    @Test
    void testUploadFile_Success() throws IOException {
        // 准备测试数据
        String originalFilename = "test.jpg";
        String content = "test image content";
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                originalFilename,
                "image/jpeg",
                content.getBytes()
        );

        // 执行测试
        FileUploadResponse response = fileUploadService.uploadFile(
                multipartFile, "avatars", 1L, "测试头像");

        // 验证结果
        assertNotNull(response);
        assertEquals(originalFilename, response.getOriginalFilename());
        assertNotNull(response.getStoredFilename());
        assertTrue(response.getStoredFilename().endsWith(".jpg"));
        assertTrue(response.getFileUrl().contains("/uploads/avatars/"));
        assertEquals(content.length(), response.getFileSize());
        assertEquals("image/jpeg", response.getFileType());
        assertEquals("avatars", response.getCategory());
        assertEquals("测试头像", response.getDescription());
        assertNotNull(response.getUploadTime());

        // 验证文件是否实际保存
        String filePath = response.getStoragePath();
        File savedFile = new File(filePath);
        assertTrue(savedFile.exists());
        assertEquals(content.length(), savedFile.length());
    }

    @Test
    void testUploadFile_FileTooLarge() {
        // 准备一个超过大小限制的文件
        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB，超过1MB限制
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // 验证抛出异常
        assertThrows(FileUploadException.class, () -> {
            fileUploadService.uploadFile(multipartFile, "avatars", 1L);
        });
    }

    @Test
    void testUploadFile_InvalidFileType() {
        // 准备一个不支持的文件类型
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.exe",
                "application/x-msdownload",
                "malicious content".getBytes()
        );

        // 验证抛出异常
        assertThrows(FileUploadException.class, () -> {
            fileUploadService.uploadFile(multipartFile, "others", 1L);
        });
    }

    @Test
    void testUploadFile_EmptyFile() {
        // 准备一个空文件
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // 验证抛出异常
        assertThrows(FileUploadException.class, () -> {
            fileUploadService.uploadFile(multipartFile, "avatars", 1L);
        });
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        // 先上传一个文件
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        FileUploadResponse response = fileUploadService.uploadFile(
                multipartFile, "avatars", 1L);

        // 验证文件存在
        String filename = response.getStoredFilename();
        assertTrue(fileUploadService.fileExists(filename, "avatars", 1L));

        // 删除文件
        boolean deleted = fileUploadService.deleteFile(filename, "avatars", 1L);

        // 验证删除成功
        assertTrue(deleted);
        assertFalse(fileUploadService.fileExists(filename, "avatars", 1L));
    }

    @Test
    void testDeleteFile_FileNotExists() {
        // 尝试删除不存在的文件
        boolean deleted = fileUploadService.deleteFile("nonexistent.jpg", "avatars", 1L);

        // 验证删除失败
        assertFalse(deleted);
    }

    @Test
    void testGetFileUrl() {
        // 测试生成文件URL
        String filename = "test123.jpg";
        String url = fileUploadService.getFileUrl(filename, "avatars", 1L);

        // 验证URL格式
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/"));
        assertTrue(url.contains("/avatars/"));
        assertTrue(url.contains("/1/"));
        assertTrue(url.endsWith(filename));
    }

    @Test
    void testFileExists() throws IOException {
        // 先上传一个文件
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        FileUploadResponse response = fileUploadService.uploadFile(
                multipartFile, "posts", 2L);

        // 验证文件存在
        String filename = response.getStoredFilename();
        assertTrue(fileUploadService.fileExists(filename, "posts", 2L));

        // 验证其他用户不能访问该文件
        assertFalse(fileUploadService.fileExists(filename, "posts", 3L));

        // 验证其他分类不能访问该文件
        assertFalse(fileUploadService.fileExists(filename, "avatars", 2L));
    }

    @Test
    void testGetStorageType() {
        // 测试获取存储类型
        String storageType = fileUploadService.getStorageType();

        // 验证存储类型
        assertEquals("local", storageType);
    }

    @Test
    void testUploadFile_WithoutUserId() throws IOException {
        // 测试不指定用户ID的上传
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        FileUploadResponse response = fileUploadService.uploadFile(
                multipartFile, "posts");

        // 验证结果
        assertNotNull(response);
        assertTrue(response.getFileUrl().contains("/uploads/posts/"));
        assertFalse(response.getFileUrl().contains("/1/")); // 不应该包含用户ID
    }

    @Test
    void testUploadFile_WithPathTraversalAttempt() {
        // 测试路径遍历攻击
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "../../etc/passwd",
                "text/plain",
                "malicious content".getBytes()
        );

        // 验证抛出异常
        assertThrows(FileUploadException.class, () -> {
            fileUploadService.uploadFile(multipartFile, "others", 1L);
        });
    }
}