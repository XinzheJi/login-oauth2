package com.system.login.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限实体类
 */
@Data
@TableName("permission")
public class Permission {
    
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 权限编码
     */
    private String code;
    
        
    /**
     * URL匹配模式
     */
    private String urlPattern;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 租户ID
     */
    private Long tenantId;

//    /**
//     * 显示名称
//     */
//    private String displayName;
//
//    /**
//     * 权限描述
//     */
//    private String description;
//
//    /**
//     * 所属模块
//     */
//    private String module;
//
//    /**
//     * 权限类型
//     */
//    private String type;
//
//    /**
//     * 父权限ID
//     */
//    private Long parentId;
//
//    /**
//     * 是否可见
//     */
//    private Boolean isVisible;

}