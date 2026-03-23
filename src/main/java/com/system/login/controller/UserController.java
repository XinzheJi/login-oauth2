package com.system.login.controller;

import com.system.login.domain.entity.User;
import com.system.login.domain.entity.Role;
import com.system.login.security.permission.RequirePermission;
import com.system.login.service.auth.UserService;
import com.system.login.service.auth.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * 提供用户管理相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    
    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping
    @RequirePermission("user:view")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.listWithRoles());
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    @RequirePermission("user:view")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        
        // 设置默认姓名为用户名（临时解决方案）
        user.setName(user.getUsername());
        
        // 添加角色信息
        List<Role> roles = roleService.getRolesByUserId(id);
        user.setRoles(roles);
        
        return ResponseEntity.ok(user);
    }
    
    /**
     * 创建新用户
     */
    @PostMapping
    @RequirePermission("user:create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // 保存姓名信息到临时变量
        String name = user.getName();
        
        userService.save(user);
        User savedUser = userService.getById(user.getId());
        
        // 还原姓名信息
        savedUser.setName(name != null && !name.isEmpty() ? name : savedUser.getUsername());
        
        // 添加角色信息
        List<Role> roles = roleService.getRolesByUserId(user.getId());
        savedUser.setRoles(roles);
        
        return ResponseEntity.ok(savedUser);
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @RequirePermission("user:update")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        // 保存姓名信息到临时变量
        String name = user.getName();
        
        user.setId(id);
        userService.updateById(user);
        
        // 获取更新后的用户
        User updatedUser = userService.getById(id);
        
        // 还原姓名信息
        updatedUser.setName(name != null && !name.isEmpty() ? name : updatedUser.getUsername());
        
        // 添加角色信息
        List<Role> roles = roleService.getRolesByUserId(id);
        updatedUser.setRoles(roles);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 根据租户ID获取用户列表
     */
    @GetMapping("/by-tenant/{tenantId}")
    @RequirePermission("user:view")
    public ResponseEntity<List<User>> getUsersByTenantId(@PathVariable Long tenantId) {
        return ResponseEntity.ok(userService.getUsersByTenantId(tenantId));
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/{userId}/roles/{roleId}")
    @RequirePermission("role:update")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        userService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 移除用户角色
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @RequirePermission("user:delete")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }
}