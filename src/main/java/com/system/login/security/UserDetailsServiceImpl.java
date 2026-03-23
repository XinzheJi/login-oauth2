package com.system.login.security;

import com.system.login.domain.entity.Permission;
import com.system.login.domain.entity.Role;
import com.system.login.mapper.PermissionMapper;
import com.system.login.mapper.RoleMapper;
import com.system.login.mapper.UserMapper;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.tenant.TenantService;
import com.system.login.domain.entity.Tenant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final TenantService tenantService;

    @Autowired
    public UserDetailsServiceImpl(UserMapper userMapper, RoleMapper roleMapper, PermissionMapper permissionMapper, TenantService tenantService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.tenantService = tenantService;
    }

    private static final String GLOBAL_TENANT_KEY = "GLOBAL";

    @Override
    @Cacheable(value = "userDetailsCache", key = "#root.target.buildCacheKey(#username)", unless = "#result == null")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("正在加载用户: {}", username);

        // 获取当前业务租户ID
        String businessTenantId = TenantContext.getCurrentTenant();
        log.debug("当前TenantContext中的业务租户ID: {}", businessTenantId);
        
        Long primaryKeyTenantId = null; // 用于数据库查询的租户主键ID
        com.system.login.domain.entity.User userEntity;

        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                primaryKeyTenantId = tenant.getId();
            } else {
                log.warn("UserDetailsServiceImpl: 无效的业务租户ID: {}, 无法加载用户", businessTenantId);
                throw new UsernameNotFoundException("租户信息无效，无法加载用户: " + username);
            }
        }

        // 根据是否有租户主键ID决定查询方式
        if (primaryKeyTenantId != null) {
            // 多租户模式，根据用户名和租户主键ID查询
            userEntity = Optional.ofNullable(userMapper.findByUsernameAndTenantId(username, primaryKeyTenantId))
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在或不属于当前租户: " + username));
        } else {
            // 非多租户模式（或TenantContext未设置），仅根据用户名查询
            userEntity = Optional.ofNullable(userMapper.findByUsername(username))
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

                // 如果是通过用户名查到的用户，且该用户有关联的租户ID (主键)，但TenantContext之前未设置
                // 则尝试将该用户的租户信息设置到TenantContext
                if (userEntity.getTenantId() != null && TenantContext.getCurrentTenant() == null) {
                    try {
                        String userBusinessTenantId = tenantService.getBusinessTenantIdByPrimaryKey(userEntity.getTenantId());
                        if (userBusinessTenantId != null) {
                            TenantContext.setCurrentTenant(userBusinessTenantId);
                            primaryKeyTenantId = userEntity.getTenantId(); // 更新 primaryKeyTenantId 供后续使用
                            log.info("UserDetailsServiceImpl: 用户 {} 属于租户 (业务ID: {}), 已设置TenantContext", username, userBusinessTenantId);
                        } else {
                            log.warn("UserDetailsServiceImpl: 用户 {} 的租户主键ID {} 没有找到对应的业务租户ID", username, userEntity.getTenantId());
                        }
                    } catch (Exception e) {
                        log.error("设置用户租户上下文时出错: {}, 租户ID: {}", username, userEntity.getTenantId(), e);
                        // 继续流程，不抛出异常
                    }
                } else if (TenantContext.getCurrentTenant() != null && userEntity.getTenantId() != null) {
                     // TenantContext已设置，用户也有租户ID，检查是否一致
                     try {
                         Tenant tenantFromContext = tenantService.getByBusinessTenantId(TenantContext.getCurrentTenant());
                         if (tenantFromContext != null && !tenantFromContext.getId().equals(userEntity.getTenantId())) {
                             log.warn("UserDetailsServiceImpl: 用户 {} (租户主键ID: {}) 与 TenantContext中的租户 (业务ID: {}, 主键ID: {}) 不一致。", 
                                 username, userEntity.getTenantId(), TenantContext.getCurrentTenant(), tenantFromContext.getId());
                              // 根据业务策略，这里可能需要抛出异常或阻止登录
                              // 但我们当前暂时不做特殊处理，避免影响现有业务流程
                         }
                         primaryKeyTenantId = userEntity.getTenantId(); // 优先使用用户自带的租户ID进行权限加载
                     } catch (Exception e) {
                         log.error("检查租户一致性时出错: {}", username, e);
                         // 继续流程，不抛出异常
                         primaryKeyTenantId = userEntity.getTenantId(); // 使用用户的租户ID
                     }
                }
            }

            log.info("成功获取用户实体: {}, 状态: {}, 租户ID: {}", userEntity.getUsername(), userEntity.getStatus(), userEntity.getTenantId());

            // 检查用户状态
            if (userEntity.getStatus() != 1) {
                log.warn("用户已禁用: {}", username);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>(); // 即使禁用用户，也可能需要加载权限以便后续判断为何禁用

            boolean enabled = (userEntity.getStatus() == 1); // 只有 status == 1 时才启用

                return new org.springframework.security.core.userdetails.User(
                        userEntity.getUsername(),
                        userEntity.getPassword(),
                        enabled, // 根据状态设置enabled标志
                        true, 
                        true,
                        true,
                        authorities 
                );
            }


        // **** 以下是用户状态正常 (status == 1) 的逻辑 ****

        // 获取用户的角色和权限
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 查询用户角色 (使用上面逻辑确定后的 primaryKeyTenantId 变量)
        List<Role> roles;
        if (primaryKeyTenantId != null) {
            roles = roleMapper.findRolesByUserIdAndTenantId(userEntity.getId(), primaryKeyTenantId);
        } else {
            roles = roleMapper.findRolesByUserId(userEntity.getId());
        }


        // 添加角色权限
        for (Role role : roles) {
            // 添加角色
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));

            // 查询角色对应的权限 (使用上面逻辑确定后的 primaryKeyTenantId 变量)
            List<Permission> permissions;
            if (primaryKeyTenantId != null) {
                permissions = permissionMapper.findPermissionsByRoleIdAndTenantId(role.getId(), primaryKeyTenantId);
            } else {
                permissions = permissionMapper.findPermissionsByRoleId(role.getId());
            }


            // 添加权限
            authorities.addAll(permissions.stream()
                    .map(p -> new SimpleGrantedAuthority(p.getCode()))
                    .collect(Collectors.toList()));
        }

        log.info("用户 {} 加载了 {} 个权限", username, authorities.size());

        // **** 返回 UserDetails (用户状态正常时) ****
        return new org.springframework.security.core.userdetails.User( // <-- 使用完整的类名
                userEntity.getUsername(),
                userEntity.getPassword(),
                true, // enabled = true 因为 status == 1
                true, // 其他标志假设为true
                true,
                true,
                authorities // 权限列表
        );
    }

    public String buildCacheKey(String username) {
        String tenant = TenantContext.getCurrentTenant();
        return (tenant == null ? GLOBAL_TENANT_KEY : tenant) + ":" + username;
    }
}
