package com.system.login.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 租户实体类
 */
@Data
@TableName("tenant")
public class Tenant {
    
    /**
     * 租户ID (数据库主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 业务租户ID
     */
    private String tenantId;
    
    /**
     * 租户编码
     */
    private String tenantCode;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 租户状态：1-启用，0-禁用
     */
    @JsonIgnore
    private Integer status = 1;
    
    /**
     * 状态文本，不映射到数据库
     */
    @TableField(exist = false)
    private String statusText;
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) {
            return "";
        }
        return status == 1 ? "启用" : "禁用";
    }
    
    /**
     * 为JSON序列化提供status字段
     * 将数据库中的整数状态(1/0)转换为前端期望的字符串状态(active/inactive)
     */
    @JsonGetter("status")
    public String getStatusForJson() {
        if (statusText != null) {
            return statusText;
        }
        return status != null && status == 1 ? "active" : "inactive";
    }
    
    /**
     * 设置状态文本
     */
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
} 