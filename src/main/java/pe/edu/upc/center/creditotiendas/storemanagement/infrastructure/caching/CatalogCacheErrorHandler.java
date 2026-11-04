package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.caching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

/**
 * Redis backs an optional cache, not a hard dependency: if it is unreachable or misconfigured,
 * requests must still work, just without caching. Spring's default error handler rethrows, which
 * would turn a Redis outage into a 500 on every catalog request — this one only logs.
 */
@Slf4j
@Component
public class CatalogCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Catalog cache '{}' unavailable on get (key={}): {}", cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Catalog cache '{}' unavailable on put (key={}): {}", cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Catalog cache '{}' unavailable on evict (key={}): {}", cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Catalog cache '{}' unavailable on clear: {}", cache.getName(), exception.getMessage());
    }
}
