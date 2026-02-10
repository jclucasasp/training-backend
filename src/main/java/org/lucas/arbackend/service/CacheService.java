package org.lucas.arbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    // Evict a specific org from the subscription cache
    public void evictSubscription(Long orgId) {
        var cache = cacheManager.getCache("active_subscriptions");
        if (cache != null) {
            cache.evict(orgId);
        }
    }

    // Evict a specific user from the auth cache
    public void evictUser(String email) {
        var cache = cacheManager.getCache("user_details");
        if (cache != null) {
            cache.evict(email);
        }
    }

    // Evict a specific API key prefix
    public void evictApiKey(String prefix) {
        var cache = cacheManager.getCache("api_keys");
        if (cache != null) {
            cache.evict(prefix);
        }
    }

    public void setCache(String cacheName, String key, Object value) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            log.info("Pre Caching new user in redis: [{}]", key);
            cache.put(key, value);
        }
    }
}