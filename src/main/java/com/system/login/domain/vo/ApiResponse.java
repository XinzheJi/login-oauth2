package com.system.login.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API响应VO
 * 符合前端要求的统一响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 状态标志
     * true表示成功，false表示失败
     */
    private Boolean state;
    
    /**
     * 错误消息
     * 当state为false时有值
     */
    private String errormsg;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 创建成功响应
     * @param data 数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .state(true)
                .data(data)
                .build();
    }
    
    /**
     * 创建成功响应（无数据）
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    /**
     * 创建失败响应
     * @param errormsg 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String errormsg) {
        return ApiResponse.<T>builder()
                .state(false)
                .errormsg(errormsg)
                .build();
    }
} 