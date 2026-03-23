package com.system.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.login.domain.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 根据角色ID查询权限ID列表
     */
    @Select("SELECT permission_id FROM role_permission WHERE role_id = #{roleId}")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID和租户ID查询权限ID列表（支持多租户）
     */
    @Select("SELECT rp.permission_id FROM role_permission rp " +
            "JOIN permission p ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.tenant_id = #{tenantId}")
    List<Long> findPermissionIdsByRoleIdAndTenantId(@Param("roleId") Long roleId, @Param("tenantId") Long tenantId);
    
    /**
     * 检查角色是否拥有指定权限
     */
    @Select("SELECT COUNT(*) FROM role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int checkRoleHasPermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    /**
     * 根据权限ID查询角色ID列表
     */
    @Select("SELECT role_id FROM role_permission WHERE permission_id = #{permissionId}")
    List<Long> findRoleIdsByPermissionId(@Param("permissionId") Long permissionId);
}