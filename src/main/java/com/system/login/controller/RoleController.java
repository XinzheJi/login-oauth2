package com.system.login.controller;

import com.system.login.domain.entity.Permission;
import com.system.login.domain.entity.Role;
import com.system.login.security.permission.RequirePermission;
import com.system.login.service.auth.PermissionService;
import com.system.login.service.auth.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色控制器
 * 提供角色管理相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;
    
    @Autowired
    public RoleController(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }
    
    /**
     * 获取所有角色列表
     */
    @GetMapping
    @RequirePermission("role:view")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.list());
    }
    
    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    @RequirePermission("role:view")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getById(id));
    }
    
    /**
     * 创建新角色
     */
    @PostMapping
    @RequirePermission("role:create")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        roleService.save(role);
        return ResponseEntity.ok(role);
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @RequirePermission("role:update")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateById(role);
        return ResponseEntity.ok(role);
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @RequirePermission("role:delete")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.removeById(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 根据用户ID获取角色列表
     */
    @GetMapping("/by-user/{userId}")
    @RequirePermission("role:view")
    public ResponseEntity<List<Role>> getRolesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(roleService.getRolesByUserId(userId));
    }
    
    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{roleId}/permissions")
    @RequirePermission("role:view")
    public ResponseEntity<List<Permission>> getRolePermissions(@PathVariable Long roleId) {
        log.info("获取角色权限列表，角色ID: {}", roleId);
        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * 为角色分配权限
     */
    @PostMapping("/{roleId}/permissions")
    @RequirePermission(value = "permission:assign", checkTenant = false)
    public ResponseEntity<Void> assignPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody Map<String, List<Long>> requestBody) {
        log.info("为角色分配权限，角色ID: {}, 请求体: {}", roleId, requestBody);
        
        // 获取权限ID列表
        List<Long> permissionIds = requestBody.get("permissionIds");
        if (permissionIds == null || permissionIds.isEmpty()) {
            log.warn("请求中未包含权限ID列表");
            return ResponseEntity.badRequest().build();
        }
        
        // 检查角色是否存在
        Role role = roleService.getById(roleId);
        if (role == null) {
            log.warn("角色不存在，ID: {}", roleId);
            return ResponseEntity.notFound().build();
        }
        
        try {
            // 清除现有权限
            permissionService.removeAllPermissionsFromRole(roleId);
            
            // 逐个验证并分配权限
            int successCount = 0;
            for (Long permissionId : permissionIds) {
                // 验证权限存在性
                Permission permission = permissionService.getById(permissionId);
                if (permission == null) {
                    log.warn("权限不存在，ID: {}, 跳过分配", permissionId);
                    continue;
                }
                
                // 分配权限
                permissionService.assignPermissionToRole(roleId, permissionId);
                successCount++;
                log.debug("成功分配权限: {} ({})", permission.getName(), permissionId);
            }
            
            log.info("已成功为角色 {} ({}) 分配 {}/{} 个权限", 
                    role.getName(), roleId, successCount, permissionIds.size());
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("为角色分配权限时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}