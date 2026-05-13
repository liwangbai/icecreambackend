package com.icecream.backend.exception;

/**
 * 未授权异常，对应HTTP 401状态码
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
