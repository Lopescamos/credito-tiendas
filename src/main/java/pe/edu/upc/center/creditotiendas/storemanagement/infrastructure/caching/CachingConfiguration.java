package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.caching;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Wires {@code @Cacheable}/{@code @CacheEvict} (US-08 catalog) to Redis: JSON values instead of
 * Java serialization, so domain aggregates don't need to implement {@code Serializable} just for
 * this, and a tolerant error handler so a Redis outage degrades to "no cache" instead of 500s.
 */
@Configuration
@EnableCaching
public class CachingConfiguration implements CachingConfigurer {

    private final CatalogCacheErrorHandler catalogCacheErrorHandler;

    public CachingConfiguration(CatalogCacheErrorHandler catalogCacheErrorHandler) {
        this.catalogCacheErrorHandler = catalogCacheErrorHandler;
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return catalogCacheErrorHandler;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            @Value("${spring.cache.redis.time-to-live:10m}") Duration timeToLive) {
        var cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(timeToLive)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return builder -> builder.cacheDefaults(cacheConfiguration);
    }
}
