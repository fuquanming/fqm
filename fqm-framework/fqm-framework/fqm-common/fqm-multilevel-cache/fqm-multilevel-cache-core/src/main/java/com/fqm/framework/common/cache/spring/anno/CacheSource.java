package com.fqm.framework.common.cache.spring.anno;

/**
 * 缓存来源
 * @version 
 * @author 傅泉明
 */
public enum CacheSource {
    /**
     * EhCache backed caching. Local
     */
    EHCACHE,

    /**
     * Caffeine backed caching. Local
     */
    CAFFEINE,
    
    /**
     * Redisson_Redis backed caching. Remote
     */
    REDISSON_REDIS,

    /**
     * Redis backed caching(jedis,Luttuce). Remote
     */
    REDIS
}
