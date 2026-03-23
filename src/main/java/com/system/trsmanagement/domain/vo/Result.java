package com.system.trsmanagement.domain.vo;

import lombok.Data;

/**
 * 通用响应结果
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 构造成功结果
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 构造成功结果（无数据）
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 构造失败结果
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 构造失败结果（默认错误码500）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
} 