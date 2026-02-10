package org.lucas.arbackend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.lucas.arbackend.exception.CustomCacheErrorHandler;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // 1. Create an ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // 2. Register the JavaTimeModule to handle LocalDateTime
        mapper.registerModules(new JavaTimeModule());

        // 3. Disable writing dates as timestamps (e.g., arrays).
        // This makes dates readable as ISO-8601 strings (e.g., "2023-10-27T10:15:30").
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // This tells Jackson to store the class name in the JSON so it can reverse it later
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 1. Default configuration (Standard 1 hour TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)));

        // 2. Specific TTLs for different business needs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // API Keys: Keep for 30 mins (Sensitive, frequent lookup)
        cacheConfigurations.put("api_keys", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // User Lookups: Keep for 1 hour
        cacheConfigurations.put("org_users", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("staff_users", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Subscriptions: Keep for 6 hours (Rarely change)
        cacheConfigurations.put("active_subscriptions", defaultConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    // Add this temporarily inside CacheConfig class
//    @Bean
//    public org.springframework.boot.CommandLineRunner clearRedis(RedisConnectionFactory connectionFactory) {
//        return args -> {
//            connectionFactory.getConnection().serverCommands().flushAll();
//            System.out.println("Redis Cache Cleared!");
//        };
//    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler(); // 2. Return your custom handler
    }
}