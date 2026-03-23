package com.system.login.security.permission;

import com.system.login.domain.entity.Permission;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.auth.PermissionService;
import com.system.login.service.auth.UserService;
import com.system.login.service.tenant.TenantService;
import com.system.login.domain.entity.Tenant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 权限拦截器
 * 用于拦截请求并检查用户是否具有所需权限
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;
    private final UserService userService;
    private final TenantService tenantService;

    @Autowired
    public PermissionInterceptor(PermissionService permissionService, UserService userService, TenantService tenantService) {
        this.permissionService = permissionService;
        this.userService = userService;
        this.tenantService = tenantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，则直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法上的RequirePermission注解
        RequirePermission methodAnnotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        // 获取类上的RequirePermission注解
        RequirePermission classAnnotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);

        // 如果方法和类上都没有RequirePermission注解，则直接放行
        if (methodAnnotation == null && classAnnotation == null) {
            return true;
        }

        // 优先使用方法上的注解，如果方法上没有，则使用类上的注解
        RequirePermission annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
        String requiredPermissionCode = annotation.value();
        boolean checkTenant = annotation.checkTenant();

        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 获取当前用户
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername();

        // 根据用户名获取用户ID
        Long userId = userService.getUserIdByUsername(username);
        if (userId == null) {
            log.error("无法找到用户ID，用户名: {}", username);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 获取用户权限列表
        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);

        // 检查用户是否具有所需权限（忽略大小写）
        boolean hasPermission = permissions.stream()
                .anyMatch(permission -> {
                    // 忽略大小写比较权限编码
                    boolean codeMatches = permission.getCode().equalsIgnoreCase(requiredPermissionCode);

                    // 如果需要检查租户，则同时检查权限编码和租户ID
                    if (checkTenant) {
                        String businessTenantId = TenantContext.getCurrentTenant();
                        Long primaryKeyTenantId = null;
                        if (businessTenantId != null) {
                            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
                            if (tenant != null) {
                                primaryKeyTenantId = tenant.getId();
                            } else {
                                log.warn("PermissionInterceptor: 无效的业务租户ID: {}, 权限检查可能不准确", businessTenantId);
                                // 根据策略，这里可能直接返回false，或者允许没有有效租户ID的系统级权限通过
                            }
                        }
                        return codeMatches &&
                                (primaryKeyTenantId == null || primaryKeyTenantId.equals(permission.getTenantId()));
                    } else {
                        // 如果不需要检查租户，则只检查权限编码
                        return codeMatches;
                    }
                });

        if (!hasPermission) {
            log.warn("用户 {} 没有所需权限 {}, 尝试访问 {}, 用户实际权限: {}",
                    userId, requiredPermissionCode, request.getRequestURI(),
                    permissions.stream().map(Permission::getCode).toList());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}