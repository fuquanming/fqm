package com.fqm.framework.cache.spring;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import com.fqm.framework.common.cache.spring.builder.CacheBuilder;
import com.fqm.framework.common.spring.util.ValueUtil;

/**
 * 多级缓存管理，使用spring@Cacheable标记
 * 1、使用一级缓存缓存null值的数据，缓存时间默认30秒
 * 2、创建多级缓存及缓存自动刷新时间（懒加载）
 * 3、缓存时间支持配置文件
 * 
    @Bean(name = "multilevelCacheRedis", destroyMethod = "destroy")
    @ConditionalOnMissingBean(value = MultilevelCacheManager.class)
    public MultilevelCacheManager multiLevelCacheManager(RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration redisCacheConfiguration) {
        MultilevelCacheManager cacheManager = new MultilevelCacheManager(this.getApplicationContext());
        List<CacheBuilder> cacheBuilders = new ArrayList<CacheBuilder>();
        cacheBuilders.add(new CaffeineCacheBuilders());
        cacheBuilders.add(new RedisCacheBuilders(redisConnectionFactory, redisCacheConfiguration));
        cacheManager.setCacheBuilders(cacheBuilders);
        return cacheManager;
    }
    
    value = "MLC|10|7"-> MLC：缓存名称，10：过期时间，5：null值过期时间，7：刷新时间
    @Cacheable(value = "MLC|10|5|7", key = "'cache_user_id_' + #id", sync = true, cacheManager = "mlcm")
    支持表达式，读取配置文件类似@Value功能
    @Cacheable(value = "MLC|${cache.user.expire}|${cache.user.nullExpire}|${cache.user.refresh}", key = "'cache_user_id_' + #id", sync = true, cacheManager = "mlcm")
 * 
 * @version 
 * @author 傅泉明
 */
public class MultilevelCacheManager extends AbstractCacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(MultilevelCacheManager.class);

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);
    
    private Collection<CacheBuilder> cacheBuilders = Collections.emptySet();
    /** 默认过期时间15分钟 */
    public static final int defaultExpireSecond = 900;
    /** 默认null值过期时间30秒 */
    public static final int defaultNullExpireSecond = 30;

    private static Timer refreshCacheTimer = new Timer(true);
    
    ApplicationContext applicationContext;
    
    public MultilevelCacheManager() {
    }
    
    public MultilevelCacheManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    protected Collection<? extends Cache> loadCaches() {
        return this.cacheMap.values();
    }
    
    public ConcurrentMap<String, Cache> getCacheMap() {
        return this.cacheMap;
    }

    @Override
    @Nullable
    public Cache getCache(String name) {
        return super.getCache(name);
    }

    public void setCacheBuilders(Collection<CacheBuilder> cacheBuilders) {
        this.cacheBuilders = cacheBuilders;
    }
    
    @Override
    protected synchronized Cache getMissingCache(String name) {
        // 创建多级缓存:name组成：cacheName|超时时间，单位秒
        String[] params = name.split("\\" + CacheBuilder.SEPARATE_CHAR);
        /** 缓存过期时间 */
        int expireSecond = 0;
        /** null值过期时间 */
        int nullExpireSecond = 0;
        /** 缓存刷新时间 */
        int refreshSecond = 0;
        
        int length = params.length;
        if (length >= 2) {
            String cacheTime = params[1];
            // 读取配置文件的数据或表达式
            Object secondObj = ValueUtil.resolveExpression((ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory(), cacheTime);
            expireSecond = Integer.valueOf(secondObj.toString());
        }
        if (length > 2) {
            Object secondObj = ValueUtil.resolveExpression((ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory(), params[2]);
            nullExpireSecond = Integer.valueOf(secondObj.toString());
        }
        if (length > 3) {
            Object secondObj = ValueUtil.resolveExpression((ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory(), params[3]);
            refreshSecond = Integer.valueOf(secondObj.toString());
        }
        
        String cacheName = CacheBuilder.getCacheName(params[0], expireSecond, nullExpireSecond, refreshSecond);;
        // lock 新创建的cache
        Cache cache = cacheMap.get(cacheName);
        if (cache != null) return cache;
        
        cache = createCache(params[0], expireSecond, nullExpireSecond, refreshSecond);
        cacheMap.put(cache.getName(), cache);
        return cache;
    }

    protected MultilevelCache createCache(String name, int expireSecond, int nullExpireSecond, int refreshSecond) {
        if (expireSecond <= 0) expireSecond = defaultExpireSecond;
        if (nullExpireSecond <= 0) nullExpireSecond = defaultNullExpireSecond;
        
        if (refreshSecond <= 0) refreshSecond = 0;
        if (refreshSecond >= expireSecond) refreshSecond = expireSecond - 5;// 过期前5秒自动刷新缓存
        
        /** 刷新时间要在过期时间的75%之后，过期时间-刷新时间>3秒，过期时间前3秒之前才会触发刷新。 */
        if (expireSecond - refreshSecond < 2) {
            logger.warn("cacheName={},expirsecond must be greater than refreshsecond for 2 seconds", CacheBuilder.getCacheName(name, expireSecond, nullExpireSecond, refreshSecond));
            refreshSecond = 0;
        } else {
            NumberFormat numberFormat = NumberFormat.getInstance();  
            numberFormat.setMaximumFractionDigits(2);
            String result = numberFormat.format((float) refreshSecond / (float) expireSecond * 100);
            if (Float.valueOf(result).intValue() < 75) {
                logger.warn("cacheName={},refreshSecond is illegal parameter, refreshSecond / expireSecond:must be more than 75%", CacheBuilder.getCacheName(name, expireSecond, nullExpireSecond, refreshSecond));
                refreshSecond = 0;
            }
        }
        
        MultilevelCache multiLevelCache = new MultilevelCache(name, expireSecond, nullExpireSecond, refreshSecond);
        multiLevelCache.setRefreshCacheTimer(refreshCacheTimer);
        
        StringBuilder data = new StringBuilder();
        data.append("createCache:").append(multiLevelCache.getName()).append(",[");
        
        // 设置第一个cache为接收null，超时时间为10秒的cache
        CacheBuilder firstCacheBuilder = cacheBuilders.iterator().next();
        Cache nullCache = firstCacheBuilder.getCache(multiLevelCache.getName(), nullExpireSecond, 0, 0);
        multiLevelCache.addCache(nullCache);
        data.append("cache=").append(firstCacheBuilder.getCacheType().name()).append("->").append(nullCache.getName()).append(",");
        
        for (CacheBuilder cacheBuilder : cacheBuilders) {
            Cache cache = cacheBuilder.getCache(multiLevelCache.getName(), expireSecond, nullExpireSecond, refreshSecond);
            multiLevelCache.addCache(cache);
            data.append("cache=").append(cacheBuilder.getCacheType().name()).append("->").append(cache.getName()).append(",");
        }
        if (cacheBuilders.size() > 0) {
            data.deleteCharAt(data.length() - 1);
        }
        data.append("]");
        
        logger.info(data.toString());
        return multiLevelCache;
    }
    
    public void destroy() {
        cacheMap.values().forEach(cache -> {
            MultilevelCache multiLevelCache = (MultilevelCache) cache;
            logger.info("cache destroy=" + multiLevelCache.getName());
            Timer refreshCacheTimer = multiLevelCache.getRefreshCacheTimer();
            if (refreshCacheTimer != null) {
                refreshCacheTimer.cancel();
            }
        });
        if (refreshCacheTimer != null) {
            logger.info("cacheTimer destroy");
            refreshCacheTimer.cancel();
        }
    }
}
