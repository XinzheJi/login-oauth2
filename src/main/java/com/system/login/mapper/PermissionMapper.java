package com.system.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.login.domain.entity.Permission;
import com.system.login.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM permission p JOIN role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId}")
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID和租户ID查询权限列表（支持多租户）
     */
    @Select("SELECT p.* FROM permission p JOIN role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId} AND p.tenant_id = #{tenantId}")
    List<Permission> findPermissionsByRoleIdAndTenantId(@Param("roleId") Long roleId, @Param("tenantId") Long tenantId);
    
    /**
     * 根据用户ID查询权限列表
     */
    @Select("SELECT DISTINCT p.* FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Permission> findPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和租户ID查询权限列表（支持多租户）
     */
    @Select("SELECT DISTINCT p.* FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND LOWER(p.code) = #{permissionCode} " +
            "AND p.tenant_id = #{tenantId} AND r.tenant_id = #{tenantId}")
    int countUserPermissionByCodeAndTenantId(@Param("userId") Long userId, @Param("permissionCode") String permissionCode, @Param("tenantId") Long tenantId);

    /**
     * 统计用户在指定租户下拥有的指定权限数量，或用户拥有的系统级权限数量
     */
    @Select("SELECT DISTINCT p.* FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "JOIN role r ON ur.role_id = r.id " + // Join role table
            "WHERE ur.user_id = #{userId} AND p.tenant_id = #{tenantId} AND r.tenant_id = #{tenantId}")
    List<Permission> findPermissionsByUserIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
    
    /**
     * 根据权限ID查询角色列表
     */
    @Select("SELECT r.* FROM role r JOIN role_permission rp ON r.id = rp.role_id WHERE rp.permission_id = #{permissionId}")
    List<Role> findRolesByPermissionId(@Param("permissionId") Long permissionId);
    
    /**
     * 根据权限ID和租户ID查询角色列表（支持多租户）
     */
    @Select("SELECT r.* FROM role r JOIN role_permission rp ON r.id = rp.role_id WHERE rp.permission_id = #{permissionId} AND r.tenant_id = #{tenantId}")
    List<Role> findRolesByPermissionIdAndTenantId(@Param("permissionId") Long permissionId, @Param("tenantId") Long tenantId);
    
    /**
     * 统计用户拥有的指定权限数量
     */
    @Select("SELECT COUNT(DISTINCT p.id) FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND LOWER(p.code) = #{permissionCode}")
    int countUserPermissionByCode(@Param("userId") Long userId, @Param("permissionCode") String permissionCode);
    


    /**
     * 统计用户在指定租户下拥有的指定权限数量，或用户拥有的系统级权限数量
     */
    @Select("SELECT COUNT(DISTINCT p.id) FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND LOWER(p.code) = #{permissionCode} " +
            "AND (p.tenant_id = #{tenantId} OR p.tenant_id IS NULL)")
    int countUserPermissionByCodeAndTenantIdOrSystem(@Param("userId") Long userId, @Param("permissionCode") String permissionCode, @Param("tenantId") Long tenantId);
}