package com.system.login.controller;

import com.system.login.domain.entity.Tenant;
import com.system.login.security.permission.RequirePermission;
import com.system.login.service.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 租户控制器
 * 提供租户管理相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;
    
    @Autowired
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }
    
    /**
     * 获取所有租户列表
     */
    @GetMapping
    @RequirePermission("tenant:view")
    public ResponseEntity<List<Tenant>> getAllTenants() {
        log.info("获取所有租户列表");
        List<Tenant> tenants = tenantService.list();
        // 转换状态为前端期望的格式
        tenants.forEach(this::convertStatusForFrontend);
        return ResponseEntity.ok(tenants);
    }
    
    /**
     * 根据ID获取租户
     */
    @GetMapping("/{id}")
    @RequirePermission("tenant:view")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        log.info("获取租户详情，ID: {}", id);
        Tenant tenant = tenantService.getById(id);
        if (tenant != null) {
            convertStatusForFrontend(tenant);
        }
        return ResponseEntity.ok(tenant);
    }
    
    /**
     * 创建新租户
     */
    @PostMapping
    @RequirePermission("tenant:create")
    public ResponseEntity<Tenant> createTenant(@RequestBody Map<String, Object> tenantData) {
        String tenantName = (String) tenantData.get("tenantName");
        String tenantCode = (String) tenantData.get("tenantCode");
        String status = (String) tenantData.get("status");
        
        log.info("创建新租户: {}", tenantName);
        
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantName);
        tenant.setTenantCode(tenantCode);
        
        // 处理状态值
        if ("active".equalsIgnoreCase(status)) {
            tenant.setStatus(1);
        } else if ("inactive".equalsIgnoreCase(status)) {
            tenant.setStatus(0);
        } else {
            // 默认为启用状态
            tenant.setStatus(1);
        }
        
        // 检查租户编码是否已存在
        if (tenantService.existsByTenantCode(tenant.getTenantCode())) {
            log.warn("租户编码已存在: {}", tenant.getTenantCode());
            return ResponseEntity.badRequest().build();
        }
        
        Tenant createdTenant = tenantService.createTenant(tenant);
        convertStatusForFrontend(createdTenant);
        return ResponseEntity.ok(createdTenant);
    }
    
    /**
     * 更新租户
     */
    @PutMapping("/{id}")
    @RequirePermission("tenant:update")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @RequestBody Map<String, Object> tenantUpdate) {
        log.info("更新租户，ID: {}", id);
        
        // 获取当前租户信息
        Tenant existingTenant = tenantService.getById(id);
        if (existingTenant == null) {
            log.warn("租户不存在，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        // 更新租户信息
        if (tenantUpdate.containsKey("tenantName")) {
            existingTenant.setTenantName((String) tenantUpdate.get("tenantName"));
        }
        // 租户编码不能被修改，因此不更新tenantCode字段
        
        // 如果请求体中包含status字段，则认为是一个状态更新
        if (tenantUpdate.containsKey("status")) {
            Object statusObj = tenantUpdate.get("status");
            Integer statusValue = null;
            
            // 处理不同类型的status值
            if (statusObj instanceof Integer) {
                statusValue = (Integer) statusObj;
            } else if (statusObj instanceof String) {
                String statusStr = (String) statusObj;
                if ("active".equalsIgnoreCase(statusStr)) {
                    statusValue = 1;
                } else if ("inactive".equalsIgnoreCase(statusStr)) {
                    statusValue = 0;
                } else {
                    try {
                        statusValue = Integer.parseInt(statusStr);
                    } catch (NumberFormatException e) {
                        log.warn("无效的状态值: {}", statusStr);
                        return ResponseEntity.badRequest().build();
                    }
                }
            }
            
            if (statusValue != null) {
                existingTenant.setStatus(statusValue);
            }
        }
        
        tenantService.updateById(existingTenant);
        convertStatusForFrontend(existingTenant);
        return ResponseEntity.ok(existingTenant);
    }
    
    /**
     * 删除租户
     */
    @DeleteMapping("/{id}")
    @RequirePermission("tenant:delete")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        log.info("删除租户，ID: {}", id);
        
        // 检查租户是否存在
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            log.warn("租户不存在，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        // 删除租户
        tenantService.removeById(id);
        log.info("租户已删除，ID: {}", id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 更新租户状态
     */
    @PutMapping("/{id}/status")
    @RequirePermission("tenant:update")
    public ResponseEntity<Tenant> updateTenantStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        log.info("更新租户状态，ID: {}", id);
        
        String status = statusUpdate.get("status");
        if (status == null) {
            log.warn("状态参数缺失");
            return ResponseEntity.badRequest().build();
        }
        
        // 获取当前租户信息
        Tenant tenant = tenantService.getById(id);
        if (tenant == null) {
            log.warn("租户不存在，ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        // 将字符串状态转换为整数值
        Integer statusValue;
        if ("active".equalsIgnoreCase(status)) {
            statusValue = 1;
        } else if ("inactive".equalsIgnoreCase(status)) {
            statusValue = 0;
        } else {
            log.warn("无效的状态值: {}", status);
            return ResponseEntity.badRequest().build();
        }
        
        // 更新状态
        tenant.setStatus(statusValue);
        tenantService.updateById(tenant);
        
        log.info("租户状态已更新为 {}，ID: {}", status, id);
        
        // 转换为前端期望的格式
        convertStatusForFrontend(tenant);
        
        return ResponseEntity.ok(tenant);
    }
    
    /**
     * 将数据库中的整数状态转换为前端期望的字符串状态
     */
    private void convertStatusForFrontend(Tenant tenant) {
        if (tenant != null && tenant.getStatus() != null) {
            int status = tenant.getStatus();
            // 为了兼容前端，在JSON序列化前修改status字段
            // 1 -> "active", 0 -> "inactive"
            tenant.setStatusText(status == 1 ? "active" : "inactive");
        }
    }
} 