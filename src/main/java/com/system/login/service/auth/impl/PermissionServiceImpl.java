package com.system.login.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.login.domain.entity.Permission;
import com.system.login.domain.entity.Role;
import com.system.login.domain.entity.RolePermission;
import com.system.login.domain.entity.Tenant;
import com.system.login.mapper.PermissionMapper;
import com.system.login.mapper.RolePermissionMapper;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.auth.PermissionService;
import com.system.login.service.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final TenantService tenantService;
    
    @Autowired
    public PermissionServiceImpl(PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper, TenantService tenantService) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.tenantService = tenantService;
    }
    
    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        // 获取当前租户ID
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                return permissionMapper.findPermissionsByRoleIdAndTenantId(roleId, tenant.getId());
            } else {
                log.warn("查询角色权限时，根据业务租户ID {} 未找到对应租户记录", businessTenantId);
                return Collections.emptyList();
            }
        } else {
            return permissionMapper.findPermissionsByRoleId(roleId);
        }
    }
    
    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        // 获取当前租户ID
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                return permissionMapper.findPermissionsByUserIdAndTenantId(userId, tenant.getId());
            } else {
                log.warn("查询用户权限时，根据业务租户ID {} 未找到对应租户记录", businessTenantId);
                return Collections.emptyList();
            }
        } else {
            return permissionMapper.findPermissionsByUserId(userId);
        }
    }
    
    @Override
    @Transactional
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        if (roleId == null || permissionId == null) {
            log.warn("角色ID或权限ID为空，无法分配权限");
            return;
        }
        
        // 检查是否已存在关联
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId)
               .eq(RolePermission::getPermissionId, permissionId);
        Integer count = Math.toIntExact(rolePermissionMapper.selectCount(wrapper));
        if (count > 0) {
            // 已存在关联，无需重复分配
            log.info(String.format("角色 %s 已拥有权限 %s", roleId, permissionId));
            return;
        }
        
        // 创建角色-权限关联
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermissionMapper.insert(rolePermission);
        log.info(String.format("为角色 %s 分配权限 %s", roleId, permissionId));
    }
    
    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        if (roleId == null || permissionId == null) {
            log.warn("角色ID或权限ID为空，无法移除权限");
            return;
        }
        
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId)
               .eq(RolePermission::getPermissionId, permissionId);
        rolePermissionMapper.delete(wrapper);
        log.info(String.format("从角色 %s 移除权限 %s", roleId, permissionId));
    }
    
    @Override
    @Transactional
    public void removeAllPermissionsFromRole(Long roleId) {
        if (roleId == null) {
            log.warn("角色ID为空，无法移除所有权限");
            return;
        }
        
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(wrapper);
        log.info(String.format("移除角色 %s 的所有权限", roleId));
    }
    
    @Override
    @Transactional
    public void removeAllRolesFromPermission(Long permissionId) {
        if (permissionId == null) {
            log.warn("权限ID为空，无法移除所有角色关联");
            return;
        }
        
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getPermissionId, permissionId);
        rolePermissionMapper.delete(wrapper);
        log.info(String.format("移除权限 %s 的所有角色关联", permissionId));
    }
    
    @Override
    public List<Role> getRolesByPermissionId(Long permissionId) {
        if (permissionId == null) {
            return Collections.emptyList();
        }
        
        // 获取当前租户ID
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                return permissionMapper.findRolesByPermissionIdAndTenantId(permissionId, tenant.getId());
            } else {
                log.warn("查询权限角色时，根据业务租户ID {} 未找到对应租户记录", businessTenantId);
                return Collections.emptyList();
            }
        } else {
            return permissionMapper.findRolesByPermissionId(permissionId);
        }
    }
    
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null || permissionCode.isEmpty()) {
            return false;
        }
        
        // 获取当前租户ID并校验权限
        String businessTenantId = TenantContext.getCurrentTenant();
        Long primaryKeyTenantId = null;
        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                primaryKeyTenantId = tenant.getId();
            } else {
                log.warn("校验权限时，根据业务租户ID {} 未找到对应租户记录", businessTenantId);
                return false; // 或者抛出异常
            }
        }
        return hasPermission(userId, permissionCode, primaryKeyTenantId);
    }
    
    @Override
    public boolean hasPermission(Long userId, String permissionCode, Long primaryKeyTenantId) {
        if (userId == null || permissionCode == null || permissionCode.isEmpty()) {
            return false;
        }
        
        // 注意：在实际的验证中，我们可能需要在数据库中查询用户的所有权限
        // 这里我们可以选择直接判断权限编码是否匹配
        int count;
        if (primaryKeyTenantId != null) {
            // Check for tenant-specific permissions or system-level permissions
            count = permissionMapper.countUserPermissionByCodeAndTenantIdOrSystem(userId, permissionCode.toLowerCase(), primaryKeyTenantId);
        } else {
            count = permissionMapper.countUserPermissionByCode(userId, permissionCode.toLowerCase());
        }
        
        return count > 0;
    }
    
    @Override
    @Transactional
    public void batchAssignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (roleId == null || permissionIds == null || permissionIds.isEmpty()) {
            log.warn("角色ID或权限ID列表为空，无法批量分配权限");
            return;
        }
        
        // 先清除该角色的所有权限
        removeAllPermissionsFromRole(roleId);
        
        // 重新分配权限
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePermission);
        }
        
        log.info(String.format("为角色 %d 个权限", roleId, permissionIds.size()));
    }
    
    @Override
    @Transactional
    public void batchAssignPermissionsToRoles(List<Long> roleIds, List<Long> permissionIds, boolean replaceExisting) {
        if (roleIds == null || roleIds.isEmpty() || permissionIds == null || permissionIds.isEmpty()) {
            log.warn("角色ID列表或权限ID列表为空，无法批量分配权限");
            return;
        }
        
        for (Long roleId : roleIds) {
            if (replaceExisting) {
                // 替换模式：先清除该角色的所有权限
                removeAllPermissionsFromRole(roleId);
            }
            
            // 分配权限
            for (Long permissionId : permissionIds) {
                assignPermissionToRole(roleId, permissionId);
            }
        }
        
        log.info(String.format("为 %d 个角色批量分配 %d 个权限", roleIds.size(), permissionIds.size()));
    }
//
//    @Override
//    public List<Permission> getPermissionsByModule(String module) {
//        if (module == null || module.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Permission::getModule, module);
//
//        // 获取当前租户ID
//        Long tenantId = TenantContext.getCurrentTenant();
//        if (tenantId != null) {
//            wrapper.eq(Permission::getTenantId, tenantId);
//        }
//
//        return list(wrapper);
//    }
//
//    @Override
//    public List<Permission> getPermissionsByType(String type) {
//        if (type == null || type.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Permission::getType, type);
//
//        // 获取当前租户ID
//        Long tenantId = TenantContext.getCurrentTenant();
//        if (tenantId != null) {
//            wrapper.eq(Permission::getTenantId, tenantId);
//        }
//
//        return list(wrapper);
//    }
}