package org.lucas.arbackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.jspecify.annotations.NonNull;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.error("Redis 'GET' failed for cache {}: {}. Falling back to DB...", cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, Object value) {
        log.error("Redis 'PUT' failed for cache {}: {}", cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log.error("Redis 'EVICT' failed for cache {}: {}", cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
        log.error("Redis 'CLEAR' failed for cache {}: {}", cache.getName(), exception.getMessage());
    }
}