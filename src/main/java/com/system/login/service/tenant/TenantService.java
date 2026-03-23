package com.system.login.service.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.system.login.domain.entity.Tenant;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.login.mapper.TenantMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 租户服务实现类
 */
@Slf4j
@Service
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {
    
    /**
     * 根据租户编码查询租户
     */
    public Tenant getByTenantCode(String tenantCode) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getTenantCode, tenantCode);
        return getOne(wrapper);
    }
    
    /**
     * 验证租户是否存在
     */
    public boolean existsByTenantCode(String tenantCode) {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getTenantCode, tenantCode);
        return count(wrapper) > 0;
    }
    
    /**
     * 创建租户
     */
    @Transactional
    public Tenant createTenant(Tenant tenant) {
        // 确保新租户默认为启用状态
        if (tenant.getStatus() == null) {
            tenant.setStatus(1);
        }
        save(tenant);
        log.info("创建租户成功: {}, 编码: {}, 状态: {}", 
            tenant.getTenantName(), tenant.getTenantCode(), tenant.getStatus());
        return tenant;
    }
    
    /**
     * 更新租户状态
     */
    @Transactional
    public Tenant updateTenantStatus(Long id, int status) {
        Tenant tenant = getById(id);
        if (tenant != null) {
            tenant.setStatus(status);
            updateById(tenant);
            log.info("租户状态已更新: {}, 新状态: {}", tenant.getTenantName(), status);
        }
        return tenant;
    }
    
    /**
     * 获取所有活跃租户
     */
    public List<Tenant> getActiveTenants() {
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getStatus, 1);
        return list(wrapper);
    }
    
    /**
     * 检查租户是否处于活跃状态
     */
    public boolean isTenantActive(Long tenantId) {
        Tenant tenant = getById(tenantId);
        return tenant != null && Integer.valueOf(1).equals(tenant.getStatus());
    }

    /**
     * 根据主键ID获取业务租户ID
     * @param primaryKeyId 主键ID (tenant.id)
     * @return 业务租户ID (tenant.tenant_id)
     */
    public String getBusinessTenantIdByPrimaryKey(Long primaryKeyId) {
        Tenant tenant = getById(primaryKeyId);
        return (tenant != null) ? tenant.getTenantId() : null;
    }

    /**
     * 根据业务租户ID查询租户实体
     * @param businessTenantId 业务租户ID (tenant.tenant_id)
     * @return Tenant 实体
     */
    public Tenant getByBusinessTenantId(String businessTenantId) {
        if (businessTenantId == null) {
            return null;
        }
        LambdaQueryWrapper<Tenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tenant::getTenantId, businessTenantId);
        return getOne(wrapper);
    }
}