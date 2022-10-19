package com.fqm.framework.common.cache.spring.builder;

import java.time.Duration;

import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.fqm.framework.common.cache.spring.anno.CacheSource;
import com.fqm.framework.common.cache.spring.anno.CacheType;


/**
 * RedisCache 构建
 * @version 
 * @author 傅泉明
 */
public class RedisCacheBuilders implements CacheBuilder {

    RedisConnectionFactory connectionFactory;
    RedisCacheConfiguration redisCacheConfiguration;

    public RedisCacheBuilders(RedisConnectionFactory connectionFactory, RedisCacheConfiguration redisCacheConfiguration) {
        this.connectionFactory = connectionFactory;
        this.redisCacheConfiguration = redisCacheConfiguration;
    }

    @Override
    public Cache getCache(String name, int expireSecond, int nullExpireSecond, int refreshSecond) {
        if (name == null) {
            name = CacheBuilder.getCacheName(CacheSource.REDIS.name(), expireSecond, nullExpireSecond, refreshSecond); 
        }

        // 默认可以保持null值
        RedisCacheConfiguration defaultCacheConfig = redisCacheConfiguration.entryTtl(Duration.ofSeconds(expireSecond));

        RedisCacheManager redisCacheManager = new RedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), 
                defaultCacheConfig);

        return redisCacheManager.getCache(name);
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.REMOTE;
    }

}
