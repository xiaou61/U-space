package com.xiaou.secure.exception;

/**
 * 安全校验异常。
 */
public class SecureException extends RuntimeException {

    public SecureException(String message) {
        super(message);
    }
}