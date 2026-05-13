package com.icecream.backend.exception;

/**
 * 文件上传异常类
 * 用于处理文件上传过程中出现的各种异常情况
 */
public class FileUploadException extends RuntimeException {

    /**
     * 构造方法
     * @param message 异常信息
     */
    public FileUploadException(String message) {
        super(message);
    }

    /**
     * 构造方法
     * @param message 异常信息
     * @param cause 原始异常
     */
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 文件大小超过限制异常
     * @param maxSize 最大允许的文件大小
     * @return FileUploadException
     */
    public static FileUploadException fileSizeExceeded(long maxSize) {
        return new FileUploadException("文件大小超过限制，最大允许" + maxSize + "字节");
    }

    /**
     * 文件类型不支持异常
     * @param allowedTypes 允许的文件类型列表
     * @return FileUploadException
     */
    public static FileUploadException fileTypeNotSupported(String allowedTypes) {
        return new FileUploadException("文件类型不支持，仅支持：" + allowedTypes);
    }

    /**
     * 文件为空异常
     * @return FileUploadException
     */
    public static FileUploadException fileIsEmpty() {
        return new FileUploadException("上传的文件为空");
    }

    /**
     * 文件存储失败异常
     * @param filename 文件名
     * @return FileUploadException
     */
    public static FileUploadException fileStorageFailed(String filename) {
        return new FileUploadException("文件存储失败：" + filename);
    }

    /**
     * 文件删除失败异常
     * @param filename 文件名
     * @return FileUploadException
     */
    public static FileUploadException fileDeleteFailed(String filename) {
        return new FileUploadException("文件删除失败：" + filename);
    }

    /**
     * 文件不存在异常
     * @param filename 文件名
     * @return FileUploadException
     */
    public static FileUploadException fileNotFound(String filename) {
        return new FileUploadException("文件不存在：" + filename);
    }

    /**
     * 路径遍历攻击检测异常
     * @param filename 文件名
     * @return FileUploadException
     */
    public static FileUploadException pathTraversalDetected(String filename) {
        return new FileUploadException("检测到路径遍历攻击：" + filename);
    }
}