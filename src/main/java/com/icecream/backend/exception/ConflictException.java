package com.icecream.backend.exception;

/**
 * 资源冲突异常，对应HTTP 409状态码
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
