package com.fqm.framework.common.cache.spring.builder;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

import com.fqm.framework.common.cache.spring.anno.CacheSource;
import com.fqm.framework.common.cache.spring.anno.CacheType;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 构建CaffeineCache
 * @version 
 * @author 傅泉明
 */
public class CaffeineCacheBuilders implements CacheBuilder {
    
    @Override
    public Cache getCache(String name, int expireSecond, int nullExpireSecond, int refreshSecond) {
        CaffeineCache caffeineCache = null;
        if (name == null) {
            name = CacheBuilder.getCacheName(CacheSource.CAFFEINE.name(), expireSecond, nullExpireSecond, refreshSecond);
        }
        caffeineCache = new CaffeineCache(name, 
                Caffeine.newBuilder().expireAfterWrite(expireSecond, TimeUnit.SECONDS).build());
        return caffeineCache;
    }
    
    @Override
    public CacheType getCacheType() {
        return CacheType.LOCAL;
    }
    
}