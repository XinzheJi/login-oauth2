package com.system.login.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色权限关联实体类
 */
@Data
@TableName("role_permission")
public class RolePermission {
    
    /**
     * 自增主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 权限ID
     */
    private Long permissionId;
}