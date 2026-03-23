package com.system.login.security.cache;

import com.system.config.CacheConfig;
import com.system.login.domain.entity.User;
import com.system.login.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 简化 UserDetails 缓存淘汰，保证调整密码后下一次认证立即生效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsCacheHelper {

    private static final String GLOBAL_TENANT_KEY = "GLOBAL";

    private final CacheManager cacheManager;
    private final TenantService tenantService;

    public void evictUser(User user) {
        if (user == null || !StringUtils.hasText(user.getUsername())) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheConfig.USER_DETAILS_CACHE);
        if (cache == null) {
            return;
        }
        evict(cache, GLOBAL_TENANT_KEY + ":" + user.getUsername());

        String businessTenantId = null;
        if (user.getTenantId() != null) {
            businessTenantId = tenantService.getBusinessTenantIdByPrimaryKey(user.getTenantId());
        }
        if (StringUtils.hasText(businessTenantId)) {
            evict(cache, businessTenantId + ":" + user.getUsername());
        }
    }

    private void evict(Cache cache, String key) {
        cache.evict(key);
        log.debug("Evicted userDetails cache entry: {}", key);
    }
}
