package com.system.ai.predict.exception;

/**
 * 封装 TDengine 访问层异常，便于上层统一处理。
 */
public class TaosAccessException extends RuntimeException {

    public TaosAccessException(String message) {
        super(message);
    }

    public TaosAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

