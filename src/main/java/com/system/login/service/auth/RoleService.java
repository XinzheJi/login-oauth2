package com.system.login.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.login.domain.entity.Role;
import com.system.login.domain.entity.UserRole;
import com.system.login.mapper.RoleMapper;
import com.system.login.mapper.UserRoleMapper;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.tenant.TenantService;
import com.system.login.domain.entity.Tenant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {
    
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final TenantService tenantService;
    
    @Autowired
    public RoleService(RoleMapper roleMapper, UserRoleMapper userRoleMapper, TenantService tenantService) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.tenantService = tenantService;
    }
    
    /**
     * 根据用户ID查询角色列表
     */
    public List<Role> getRolesByUserId(Long userId) {
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                return roleMapper.findRolesByUserIdAndTenantId(userId, tenant.getId());
            } else {
                log.warn("查询用户角色时，根据业务租户ID {} 未找到对应租户记录", businessTenantId);
                return List.of();
            }
        } else {
            return roleMapper.findRolesByUserId(userId);
        }
    }
    
    /**
     * 为用户分配角色
     */
    public void assignRoleToUser(Long userId, Long roleId) {
        // 先检查该用户是否已经拥有这个角色
        LambdaQueryWrapper<UserRole> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserRole::getUserId, userId)
                  .eq(UserRole::getRoleId, roleId);
        Long count = userRoleMapper.selectCount(checkWrapper);
        if (count > 0) {
            log.info("用户 {} 已经拥有角色 {}, 跳过分配", userId, roleId);
            return;
        }
        
        // 不存在则插入新的关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);
        log.info("为用户 {} 分配角色 {}", userId, roleId);
    }
    
    /**
     * 移除用户角色
     */
    public void removeRoleFromUser(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId)
               .eq(UserRole::getRoleId, roleId);
        userRoleMapper.delete(wrapper);
        log.info("移除用户 {} 的角色 {}", userId, roleId);
    }
}