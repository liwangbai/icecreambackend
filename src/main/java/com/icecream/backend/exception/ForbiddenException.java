package com.icecream.backend.exception;

/**
 * 禁止访问异常，对应HTTP 403状态码
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
