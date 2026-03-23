//package com.system.login.config;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.system.login.domain.entity.Permission;
//import com.system.login.domain.entity.Role;
//import com.system.login.domain.entity.Tenant;
//import com.system.login.domain.entity.User;
//import com.system.login.mapper.UserMapper;
//import com.system.login.security.tenant.TenantContext;
//import com.system.login.service.auth.PermissionService;
//import com.system.login.service.auth.RoleService;
//import com.system.login.service.auth.UserService;
//import com.system.login.service.tenant.TenantService;
//import com.system.login.mapper.PermissionMapper;
//import com.system.login.mapper.TenantMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * 数据初始化器
// * 用于在应用启动时初始化测试数据
// * 仅在开发和测试环境中启用
// */
//@Slf4j
//@Component
//@Profile({"dev", "test"})
//public class DataInitializer implements CommandLineRunner {
//
//    private final TenantService tenantService;
//    private final UserService userService;
//    private final RoleService roleService;
//    private final PermissionService permissionService;
//    private final PermissionMapper permissionMapper;
//    private final TenantMapper tenantMapper;
//    private final UserMapper userMapper;
//
//    @Autowired
//    public DataInitializer(TenantService tenantService, UserService userService,
//                           RoleService roleService, PermissionService permissionService,
//                           PermissionMapper permissionMapper, TenantMapper tenantMapper, UserMapper userMapper) {
//        this.tenantService = tenantService;
//        this.userService = userService;
//        this.roleService = roleService;
//        this.permissionService = permissionService;
//        this.permissionMapper = permissionMapper;
//        this.tenantMapper = tenantMapper;
//        this.userMapper = userMapper;
//    }
//
//    @Override
//    public void run(String... args) {
//        log.info("开始初始化测试数据...");
//
//        // 创建租户
//        createTenants();
//
//        // 创建权限
//        createPermissions();
//
//        // 创建角色
//        createRoles();
//
//        // 创建用户
//        createUsers();
//
//        // 分配角色和权限
//        assignRolesAndPermissions();
//
//        log.info("测试数据初始化完成");
//    }
//
//    private void createTenants() {
//        // 创建两个测试租户
//        createTenantIfNotExists("租户A", "TENANT_A");
//        createTenantIfNotExists("租户B", "TENANT_B");
//
//        log.info("租户初始化完成，已跳过已存在的租户数据");
//    }
//
//    private void createPermissions() {
//        // 清除租户上下文，确保系统级权限以null tenantId插入
//        TenantContext.clear();
//
//        // 系统级权限（不属于特定租户）
//        createPermission(null, "用户查看", "user:view", "/api/users/**", "GET");
//        createPermission(null, "用户创建", "user:create", "/api/users", "POST");
//        createPermission(null, "用户更新", "user:update", "/api/users/**", "PUT");
//        createPermission(null, "用户删除", "user:delete", "/api/users/**", "DELETE");
//        createPermission(null, "用户角色分配", "user:assign-role", "/api/users/*/roles/*", "POST");
//        createPermission(null, "用户角色移除", "user:remove-role", "/api/users/*/roles/*", "DELETE");
//
//        createPermission(null, "角色查看", "role:view", "/api/roles/**", "GET");
//        createPermission(null, "角色创建", "role:create", "/api/roles", "POST");
//        createPermission(null, "角色更新", "role:update", "/api/roles/**", "PUT");
//        createPermission(null, "角色删除", "role:delete", "/api/roles/**", "DELETE");
//
//        createPermission(null, "权限查看", "permission:view", "/api/permissions/**", "GET");
//        createPermission(null, "权限创建", "permission:create", "/api/permissions", "POST");
//        createPermission(null, "权限更新", "permission:update", "/api/permissions/**", "PUT");
//        createPermission(null, "权限删除", "permission:delete", "/api/permissions/**", "DELETE");
//        createPermission(null, "权限分配", "permission:assign", "/api/permissions/assign", "POST");
//        createPermission(null, "权限移除", "permission:remove", "/api/permissions/remove", "DELETE");
//
//        // 添加租户管理权限
//        createPermission(null, "租户查看", "tenant:view", "/api/tenants/**", "GET");
//        createPermission(null, "租户创建", "tenant:create", "/api/tenants", "POST");
//        createPermission(null, "租户更新", "tenant:update", "/api/tenants/**", "PUT");
//        createPermission(null, "租户删除", "tenant:delete", "/api/tenants/**", "DELETE");
//
//        // 获取租户ID
//        Tenant tenantA = getTenantByCode("TENANT_A");
//        Tenant tenantB = getTenantByCode("TENANT_B");
//
//        // 租户A的特定权限
//        if (tenantA != null) {
//            createPermission(tenantA.getId(), "租户A数据查看", "tenant_a:data:view", "/api/tenant-a/data/**", "GET");
//            createPermission(tenantA.getId(), "租户A数据管理", "tenant_a:data:manage", "/api/tenant-a/data/**", "*");
//        }
//
//        // 租户B的特定权限
//        if (tenantB != null) {
//            createPermission(tenantB.getId(), "租户B数据查看", "tenant_b:data:view", "/api/tenant-b/data/**", "GET");
//            createPermission(tenantB.getId(), "租户B数据管理", "tenant_b:data:manage", "/api/tenant-b/data/**", "*");
//        }
//
//        log.info("创建权限完成");
//    }
//
//    private Tenant getTenantByCode(String tenantCode) {
//        LambdaQueryWrapper<Tenant> tenantWrapper = new LambdaQueryWrapper<>();
//        tenantWrapper.eq(Tenant::getTenantCode, tenantCode);
//        return tenantMapper.selectOne(tenantWrapper);
//    }
//
//    private void createPermission(Long tenantId, String name, String code, String urlPattern, String method) {
//        // Check if permission already exists
//        LambdaQueryWrapper<Permission> permissionWrapper = new LambdaQueryWrapper<>();
//        permissionWrapper.eq(Permission::getCode, code);
//        if (tenantId != null) {
//            permissionWrapper.eq(Permission::getTenantId, tenantId);
//        } else {
//             // Ensure tenantId is null in the query for system-level permissions
//             permissionWrapper.isNull(Permission::getTenantId);
//        }
//        Permission existingPermission = permissionMapper.selectOne(permissionWrapper);
//        boolean exists = existingPermission != null;
//
//        if (!exists) {
//            Permission permission = new Permission();
//            permission.setName(name);
//            permission.setCode(code);
//            permission.setUrlPattern(urlPattern);
//            permission.setMethod(method);
//            permission.setTenantId(tenantId);
//            permissionService.save(permission);
//        }
//    }
//
//    private void createRoles() {
//        // 系统级角色
//        createRole(null, "超级管理员", "SUPER_ADMIN");
//        createRole(null, "系统管理员", "SYSTEM_ADMIN");
//
//        // 租户A的角色
//        createRole(1L, "租户A管理员", "TENANT_A_ADMIN");
//        createRole(1L, "租户A普通用户", "TENANT_A_USER");
//
//        // 租户B的角色
//        createRole(2L, "租户B管理员", "TENANT_B_ADMIN");
//        createRole(2L, "租户B普通用户", "TENANT_B_USER");
//
//        log.info("创建角色完成");
//    }
//
//    private void createTenantIfNotExists(String tenantName, String tenantCode) {
//        LambdaQueryWrapper<Tenant> tenantWrapper = new LambdaQueryWrapper<>();
//        tenantWrapper.eq(Tenant::getTenantCode, tenantCode);
//        long count = tenantMapper.selectCount(tenantWrapper);
//        boolean exists = count > 0;
//
//        if (!exists) {
//            Tenant tenant = new Tenant();
//            tenant.setTenantName(tenantName);
//            tenant.setTenantCode(tenantCode);
//            tenant.setStatus(String.valueOf(1));
//            tenantService.save(tenant);
//        }
//    }
//
//    private void createRole(Long tenantId, String name, String code) {
//        Role role = new Role();
//        role.setName(name);
//        role.setCode(code);
//        role.setTenantId(tenantId);
//        roleService.save(role);
//    }
//
//    private void createUsers() {
//        // 系统级用户
//        createUser(null, "admin", "admin123", "超级管理员");
//
//        // 租户A的用户
//        createUser(1L, "tenant_a_admin", "password", "租户A管理员");
//        createUser(1L, "tenant_a_user", "password", "租户A用户");
//
//        // 租户B的用户
//        createUser(2L, "tenant_b_admin", "password", "租户B管理员");
//        createUser(2L, "tenant_b_user", "password", "租户B用户");
//
//        log.info("创建用户完成");
//    }
//
//    private void createUser(Long tenantId, String username, String password, String nickname) {
//        // Check if user already exists
//        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
//        userWrapper.eq(User::getUsername, username);
//        if (tenantId != null) {
//            userWrapper.eq(User::getTenantId, tenantId);
//        } else {
//            userWrapper.isNull(User::getTenantId);
//        }
//        long count = userMapper.selectCount(userWrapper);
//        boolean exists = count > 0;
//
//        if (!exists) {
//            User user = new User();
//            user.setUsername(username);
//            user.setPassword(password);
//            // nickname参数在这里只用于日志记录，不设置到实体中
//            // 使用status字段表示用户状态：1-正常，0-禁用
//            user.setStatus(1); // 1表示启用
//            user.setTenantId(tenantId);
//            userService.save(user);
//        } else {
//            log.info("用户 {} (租户ID: {}) 已存在，跳过创建", username, tenantId);
//        }
//    }
//
//    private void assignRolesAndPermissions() {
//        // 为超级管理员分配所有系统权限
//        Role superAdminRole = roleService.getOne(roleService.lambdaQuery().eq(Role::getCode, "SUPER_ADMIN"));
//        List<Permission> allPermissions = permissionService.list();
//        for (Permission permission : allPermissions) {
//            permissionService.assignPermissionToRole(superAdminRole.getId(), permission.getId());
//        }
//
//        // 为系统管理员分配系统管理权限
//        Role systemAdminRole = roleService.getOne(roleService.lambdaQuery().eq(Role::getCode, "SYSTEM_ADMIN"));
//        List<Permission> systemPermissions = permissionService.list(permissionService.lambdaQuery().isNull(Permission::getTenantId));
//        for (Permission permission : systemPermissions) {
//            permissionService.assignPermissionToRole(systemAdminRole.getId(), permission.getId());
//        }
//
//        // 为租户A管理员分配租户A的所有权限
//        Role tenantAAdminRole = roleService.getOne(roleService.lambdaQuery().eq(Role::getCode, "TENANT_A_ADMIN"));
//        List<Permission> tenantAPermissions = permissionService.list(permissionService.lambdaQuery().eq(Permission::getTenantId, 1L));
//        for (Permission permission : tenantAPermissions) {
//            permissionService.assignPermissionToRole(tenantAAdminRole.getId(), permission.getId());
//        }
//
//        // 为租户A普通用户分配查看权限
//        Role tenantAUserRole = roleService.getOne(roleService.lambdaQuery().eq(Role::getCode, "TENANT_A_USER"));
//        Permission tenantAViewPermission = permissionService.getOne(permissionService.lambdaQuery()
//                .eq(Permission::getCode, "tenant_a:data:view"));
//        permissionService.assignPermissionToRole(tenantAUserRole.getId(), tenantAViewPermission.getId());
//
//        // 为租户B管理员分配租户B的所有权限
//        Role tenantBAdminRole = roleService.getOne(roleService.lambdaQuery().eq(Role::getCode, "TENANT_B_ADMIN"));
//        List<Permission> tenantBPermissions = permissionService.list(permissionService.lambdaQuery().eq(Permission::getTenantId, 2L));
//        for (Permission permission : tenantBPermissions) {
//            permissionService.assignPermissionToRole(tenantBAdminRole.getId(), permission.getId());
//        }
//
//        // 为租户B普通用户分配查看权限
//        Role tenantBUserRole = roleService.getOne(roleService.lambdaQuery().eq(Role::getCode, "TENANT_B_USER"));
//        Permission tenantBViewPermission = permissionService.getOne(permissionService.lambdaQuery()
//                .eq(Permission::getCode, "tenant_b:data:view"));
//        permissionService.assignPermissionToRole(tenantBUserRole.getId(), tenantBViewPermission.getId());
//
//        // 为用户分配角色
//        User adminUser = userService.getByUsername("admin");
//        userService.assignRoleToUser(adminUser.getId(), superAdminRole.getId());
//
//        User tenantAAdminUser = userService.getByUsername("tenant_a_admin");
//        userService.assignRoleToUser(tenantAAdminUser.getId(), tenantAAdminRole.getId());
//
//        User tenantAUser = userService.getByUsername("tenant_a_user");
//        userService.assignRoleToUser(tenantAUser.getId(), tenantAUserRole.getId());
//
//        User tenantBAdminUser = userService.getByUsername("tenant_b_admin");
//        userService.assignRoleToUser(tenantBAdminUser.getId(), tenantBAdminRole.getId());
//
//        User tenantBUser = userService.getByUsername("tenant_b_user");
//        userService.assignRoleToUser(tenantBUser.getId(), tenantBUserRole.getId());
//
//        log.info("角色和权限分配完成");
//    }
//}