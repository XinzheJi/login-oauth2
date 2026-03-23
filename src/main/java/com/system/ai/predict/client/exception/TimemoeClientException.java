package com.system.ai.predict.client.exception;

import lombok.Getter;

/**
 * timemoe 客户端调用异常。
 */
@Getter
public class TimemoeClientException extends RuntimeException {

    private final ErrorType errorType;
    private final Integer statusCode;
    private final String path;
    private final String responseBody;

    public TimemoeClientException(ErrorType errorType,
                                  Integer statusCode,
                                  String path,
                                  String responseBody,
                                  String message,
                                  Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.statusCode = statusCode;
        this.path = path;
        this.responseBody = responseBody;
    }

    public enum ErrorType {
        CLIENT_ERROR,
        SERVER_ERROR,
        NETWORK_ERROR,
        UNKNOWN
    }
}
