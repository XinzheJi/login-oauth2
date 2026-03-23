package com.system.login.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 姓名
     */
    @TableField(exist = false)
    private String name;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 用户角色列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Role> roles;
} 