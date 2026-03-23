package com.system.ai.predict.controller.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 预测模块通用响应。
 */
@Getter
@Setter
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private Boolean success;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage("OK");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> failure(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> failure(String message) {
        return failure(500, message);
    }
}
