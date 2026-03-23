package com.system.login.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.system.login.domain.entity.Tenant;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;

/**
 * MyBatis-Plus配置类，实现多租户数据隔离和分页功能
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 不需要进行多租户隔离的表
     */
    private static final List<String> IGNORE_TENANT_TABLES = Arrays.asList(
            "tenant", "user_role", "role_permission", "permission"
    );

    /**
     * 配置MybatisPlus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(@Lazy TenantService tenantService) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加多租户插件，自动为SQL添加租户条件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                // 从当前线程上下文获取业务租户ID
                String businessTenantId = TenantContext.getCurrentTenant();
                Long primaryKeyTenantId = 1L; // 默认值或错误处理值

                if (businessTenantId != null) {
                    Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
                    if (tenant != null) {
                        primaryKeyTenantId = tenant.getId();
                    } else {
                        log.warn("MybatisPlusConfig: 无效的业务租户ID: {}, 将使用默认租户ID: {}", businessTenantId, primaryKeyTenantId);
                        // 注意: 在拦截器中直接抛出异常可能会中断所有数据库操作，需要谨慎处理
                    }
                }
                return new LongValue(primaryKeyTenantId);
            }
            
            @Override
            public String getTenantIdColumn() {
                // 租户ID的列名
                return "tenant_id";
            }
            
            @Override
            public boolean ignoreTable(String tableName) {
                // 指定哪些表不需要进行多租户隔离
                return IGNORE_TENANT_TABLES.contains(tableName);
            }
        }));
        
        // 添加分页插件，必须放在租户插件之后
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setMaxLimit(100L); // 设置最大单页限制数量，防止查询过多数据
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}