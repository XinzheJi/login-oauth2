package com.system.login.service.auth;

import com.baomidou.mybatisplus.extension.service.IService;
import com.system.login.domain.entity.Permission;
import com.system.login.domain.entity.Role;

import java.util.List;

/**
 * 权限服务接口
 * 提供权限管理相关功能
 */
public interface PermissionService extends IService<Permission> {
    
    /**
     * 根据角色ID获取权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);
    
    /**
     * 根据用户ID获取权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);
    
    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     */
    void assignPermissionToRole(Long roleId, Long permissionId);
    
    /**
     * 从角色中移除权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     */
    void removePermissionFromRole(Long roleId, Long permissionId);
    
    /**
     * 移除角色的所有权限
     * @param roleId 角色ID
     */
    void removeAllPermissionsFromRole(Long roleId);
    
    /**
     * 移除权限的所有角色关联
     * @param permissionId 权限ID
     */
    void removeAllRolesFromPermission(Long permissionId);
    
    /**
     * 根据权限ID获取角色列表
     * @param permissionId 权限ID
     * @return 角色列表
     */
    List<Role> getRolesByPermissionId(Long permissionId);
    
    /**
     * 判断用户是否拥有指定权限
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionCode);
    
    /**
     * 判断用户是否拥有指定权限（考虑租户隔离）
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @param tenantId 租户ID
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionCode, Long tenantId);
    
    /**
     * 批量为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds);
    
    /**
     * 批量为多个角色分配多个权限
     * @param roleIds 角色ID列表
     * @param permissionIds 权限ID列表
     * @param replaceExisting 是否替换现有权限
     */
    void batchAssignPermissionsToRoles(List<Long> roleIds, List<Long> permissionIds, boolean replaceExisting);
//
//    /**
//     * 根据模块获取权限列表
//     * @param module 模块名称
//     * @return 权限列表
//     */
//    List<Permission> getPermissionsByModule(String module);
//
//    /**
//     * 根据权限类型获取权限列表
//     * @param type 权限类型
//     * @return 权限列表
//     */
//    List<Permission> getPermissionsByType(String type);
} 