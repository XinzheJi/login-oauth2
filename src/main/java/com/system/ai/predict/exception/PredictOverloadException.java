package com.system.ai.predict.exception;

/**
 * 预测服务过载异常。
 */
public class PredictOverloadException extends RuntimeException {

    public PredictOverloadException(String message) {
        super(message);
    }
}
