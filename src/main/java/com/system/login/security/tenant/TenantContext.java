package com.system.login.security.tenant;

/**
 * 租户上下文，用于在线程中存储当前租户ID
 */
public class TenantContext {
    
    private static final ThreadLocal<String> CURRENT_TENANT = new InheritableThreadLocal<>();
    
    /**
     * 设置当前租户ID
     */
    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
    
    /**
     * 获取当前租户ID
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }
    
    /**
     * 清除当前租户ID
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}