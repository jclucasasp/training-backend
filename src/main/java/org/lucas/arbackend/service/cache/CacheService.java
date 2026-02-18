package org.lucas.arbackend.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public void updateCache(String cacheName, String key, Object value) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null)
            cache.put(key, value);
    }

    // Evict a specific org from the subscription cache
    public void evictSubscription(Long orgId) {
        var cache = cacheManager.getCache("active_subscriptions");
        if (cache != null) {
            cache.evict(orgId);
        }
    }

    // Evict a specific user from the auth cache
    public void evictAuthUser(String email) {
        var cache = cacheManager.getCache("auth_user");
        if (cache != null)
            cache.evict(email);
    }

    // Evict a specific API key prefix
    public void evictApiKey(String prefix) {
        var cache = cacheManager.getCache("api_keys");
        if (cache != null) {
            cache.evict(prefix);
        }
    }

}