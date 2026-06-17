package org.lucas.arbackend.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.security.StudentTokenResponse;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public StudentTokenResponse getActiveStudentToken(String studentNumber) {
        var cache = cacheManager.getCache("student_token");
        return (cache != null) ? cache.get(studentNumber, StudentTokenResponse.class) : null;
    }

    public void evictActiveStudentToken(String studentNumber) {
        var cache = cacheManager.getCache("student_token");
        if (cache != null)
            cache.evict(studentNumber);
    }

    public void updateCache(String cacheName, String key, Object value) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null)
            cache.put(key, value);
    }

    // Evict a specific org from the subscription cache
    public void evictSubscription(Long orgId) {
        var cache = cacheManager.getCache("active_subscription");
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
        var cache = cacheManager.getCache("api_key");
        if (cache != null) {
            cache.evict(prefix);
        }
    }

}