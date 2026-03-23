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
 * 权限控制器
 * 提供权限管理相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final RoleService roleService;

    @Autowired
    public PermissionController(PermissionService permissionService, RoleService roleService) {
        this.permissionService = permissionService;
        this.roleService = roleService;
    }

    /**
     * 获取所有权限列表
     */
    @GetMapping
    @RequirePermission("permission:view")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.list());
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    @RequirePermission("permission:view")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getById(id));
    }

    /**
     * 创建新权限
     */
    @PostMapping
    @RequirePermission("permission:create")
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        permissionService.save(permission);
        return ResponseEntity.ok(permission);
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @RequirePermission("permission:update")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        permissionService.updateById(permission);
        return ResponseEntity.ok(permission);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @RequirePermission("permission:delete")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.removeById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据角色ID获取权限列表
     */
    @GetMapping("/by-role/{roleId}")
    @RequirePermission("permission:view")
    public ResponseEntity<List<Permission>> getPermissionsByRoleId(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.getPermissionsByRoleId(roleId));
    }

    /**
     * 根据用户ID获取权限列表
     */
    @GetMapping("/by-user/{userId}")
    @RequirePermission("permission:view")
    public ResponseEntity<List<Permission>> getPermissionsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionService.getPermissionsByUserId(userId));
    }

    /**
     * 获取所有角色列表（用于权限分配）
     */
    @GetMapping("/roles")
    @RequirePermission("permission:view")
    public ResponseEntity<List<Role>> getAllRolesForPermission() {
        return ResponseEntity.ok(roleService.list());
    }

    /**
     * 为角色分配权限
     */
    @PostMapping("/assign")
    @RequirePermission("permission:assign")
    public ResponseEntity<Void> assignPermissionsToRole(@RequestBody Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) params.get("permissionIds");

        // 先清除该角色的所有权限
        permissionService.removeAllPermissionsFromRole(roleId);

        // 重新分配权限
        for (Long permissionId : permissionIds) {
            permissionService.assignPermissionToRole(roleId, permissionId);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/role/{roleId}")
    @RequirePermission("permission:view")
    public ResponseEntity<List<Permission>> getRolePermissions(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.getPermissionsByRoleId(roleId));
    }

    /**
     * 获取权限的角色列表
     */
    @GetMapping("/{permissionId}/roles")
    @RequirePermission("permission:view")
    public ResponseEntity<List<Role>> getPermissionRoles(@PathVariable Long permissionId) {
        return ResponseEntity.ok(permissionService.getRolesByPermissionId(permissionId));
    }

    /**
     * 为权限分配角色
     */
    @PostMapping("/{permissionId}/roles")
    @RequirePermission(value = "permission:assign", checkTenant = false)
    public ResponseEntity<Void> assignRolesToPermission(
            @PathVariable Long permissionId,
            @RequestBody Map<String, List<Long>> params) {
        log.info("为权限分配角色，权限ID: {}, 参数: {}", permissionId, params);
        
        List<Long> roleIds = params.get("roleIds");
        if (roleIds == null || roleIds.isEmpty()) {
            log.warn("请求中未包含角色ID列表");
            return ResponseEntity.badRequest().build();
        }
        
        // 检查权限是否存在
        Permission permission = permissionService.getById(permissionId);
        if (permission == null) {
            log.warn("权限不存在，ID: {}", permissionId);
            return ResponseEntity.notFound().build();
        }

        try {
            // 先清除该权限的所有角色关联
            permissionService.removeAllRolesFromPermission(permissionId);

            // 重新分配角色
            for (Long roleId : roleIds) {
                // 验证角色存在性
                Role role = roleService.getById(roleId);
                if (role == null) {
                    log.warn("角色不存在，ID: {}, 跳过分配", roleId);
                    continue;
                }
                permissionService.assignPermissionToRole(roleId, permissionId);
            }
            
            log.info("已成功为权限{}(名称:{})分配{}个角色", permissionId, permission.getName(), roleIds.size());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("为权限分配角色时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量为权限分配角色
     */
    @PostMapping("/batch-assign")
    @RequirePermission("permission:assign")
    public ResponseEntity<Void> batchAssignRolesToPermissions(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) params.get("permissionIds");
        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) params.get("roleIds");
        String mode = params.get("mode").toString();

        for (Long permissionId : permissionIds) {
            if ("replace".equals(mode)) {
                // 替换模式：先清除该权限的所有角色关联
                permissionService.removeAllRolesFromPermission(permissionId);
            }

            // 分配新角色
            for (Long roleId : roleIds) {
                permissionService.assignPermissionToRole(roleId, permissionId);
            }
        }

        return ResponseEntity.ok().build();
    }
    public ResponseEntity<Void> assignPermissionToRole(
            @RequestParam Long roleId,
            @RequestParam Long permissionId) {
        permissionService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    /**
     * 移除角色权限
     */
    @DeleteMapping("/remove")
    @RequirePermission("permission:remove")
    public ResponseEntity<Void> removePermissionFromRole(
            @RequestParam Long roleId,
            @RequestParam Long permissionId) {
        permissionService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }
}