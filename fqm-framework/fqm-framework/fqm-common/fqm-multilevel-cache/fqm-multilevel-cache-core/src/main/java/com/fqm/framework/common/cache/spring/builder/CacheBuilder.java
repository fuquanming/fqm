package com.fqm.framework.common.cache.spring.builder;

import org.springframework.cache.Cache;

import com.fqm.framework.common.cache.spring.anno.CacheType;


/**
 * 
 * @version 
 * @author 傅泉明
 */
public interface CacheBuilder {
    /** 分隔符 */
    public static final String SEPARATE_CHAR = "|";

    public static final String NULL_CHAR = "null";

    /**
     * 创建缓存
     * @param name              缓存名称
     * @param expireSecond      过期时间，单位：秒
     * @param nullExpireSecond  null值过期时间，单位：秒
     * @param refreshSecond     刷新时间，单位：秒
     * @param name              缓存名称
     * @return
     */
    public Cache getCache(String name, int expireSecond, int nullExpireSecond, int refreshSecond);

    public CacheType getCacheType();

    public static String getCacheName(String name, int expireSecond, int nullExpireSecond, int refreshSecond) {
        return name + SEPARATE_CHAR + expireSecond + SEPARATE_CHAR + nullExpireSecond + SEPARATE_CHAR + refreshSecond;
    }
}
