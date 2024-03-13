package com.fqm.framework.cache.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import com.fqm.framework.common.cache.spring.builder.CacheBuilder;

/**
 * 多级缓存
 *  -父类：AbstractValueAdaptingCache->allowNullValues为false，将不能缓存数据，会报异常，详见方法：toStoreValue
 *  -this.name=MLC|超时时间|刷新时间，单位：秒
 * 1、在获取到缓存的时候：如果达到刷新时间则，异步刷新缓存，过期时间前3秒之前才刷新
 * @version 
 * @author 傅泉明
 */
public class MultilevelCache extends AbstractValueAdaptingCache {
    
    public static final String NAME = "MLC";
    /** 
     * name=MLC|超时时间|刷新时间，单位：秒
     * MLC|10：超时10秒
     */
    private String cacheName;
    private List<Cache> cacheList = new ArrayList<>();
    private int expireSecond;
    private int refreshSecond;
    /**
     * 缓存调用的方法，用于刷新缓存
     * 1、在获取到缓存的时候：如果达到刷新时间则，异步刷新缓存
     */
    private Map<Object, CacheRefresh> cacheCallMap = new ConcurrentHashMap<>();
    /**
     * 待刷新缓存的key
     */
    private Map<Object, Boolean> refreshKeyMap = new ConcurrentHashMap<>();
    
    private ScheduledExecutorService refreshCacheTimer = null;
    
    public MultilevelCache(String name, int expireSecond, int nullExpireSecond, int refreshSecond) {
        super(true);
        this.cacheName = name;
        this.cacheName = buildName(expireSecond, nullExpireSecond, refreshSecond);
    }
    
    public MultilevelCache(int expireSecond, int nullExpireSecond, int refreshSecond) {
        super(true);
        this.cacheName = buildName(expireSecond, nullExpireSecond, refreshSecond);
    }
    
    public String buildName(int expireSecond, int nullExpireSecond, int refreshSecond) {
        this.expireSecond = expireSecond;
        this.refreshSecond = refreshSecond;
        return CacheBuilder.getCacheName(this.cacheName == null ? NAME : this.cacheName, expireSecond, nullExpireSecond, refreshSecond);
    }
    
    public MultilevelCache addCache(Cache cache) {
        cacheList.add(cache);
        return this;
    }
    
    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }
    
    @Override
    public ValueWrapper get(Object key) {
        int size = cacheList.size();
        int cacheIndex = 0;
        ValueWrapper vw = null;
        for (int i = 0; i < size; i++) {
            Cache cache = cacheList.get(i);
            cacheIndex = i;
            vw = cache.get(key);
            if (vw != null) {
                // 刷新缓存
                refreshCache(key);
                break;
            }
        }
        
        if (vw != null) {
            for (int i = 0; i < cacheIndex; i++) {
                // 拷贝缓存
                Cache cache = cacheList.get(i);
                cache.put(key, vw.get());
            }
            return vw;
        }
        return null;
    }
    
    ReentrantLock createDatalock = new ReentrantLock();
    
    @SuppressWarnings("unchecked")
    @Override
    /**
     * cache 使用 sync=true，只能一个线程加载数据
     * @see org.springframework.cache.Cache#get(java.lang.Object, java.util.concurrent.Callable)
     *
     */
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper vw = get(key);
        if (vw != null) {
            return (T) vw.get();
        }
        
        createDatalock.lock();
        try {
            vw = get(key);
            if (vw != null) {
                return (T) vw.get();
            }
            // 执行缓存调用的方法
            T obj = valueLoader.call();
            
            saveCacheRefresh(key, valueLoader);
            // obj == null，设置第一个cache存储null
            if (obj == null) {
                cacheList.get(0).put(key, obj);
            } else {
                put(key, obj);
            }
            
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            createDatalock.unlock();
        }
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        cacheList.forEach(cache ->
            cache.put(key, value)
        );
    }
    
    @Override
    public void evict(Object key) {
        cacheList.forEach(cache ->
            cache.evict(key)
        );
    }

    @Override
    public void clear() {
        cacheList.forEach(Cache::clear);
    }

    @Override
    protected Object lookup(Object key) {
        ValueWrapper vw = get(key);
        if (vw != null) {
            return vw.get();
        }
        return null;
    }
    
    public int getExpireSecond() {
        return expireSecond;
    }
    public int getRefreshSecond() {
        return refreshSecond;
    }
    public Map<Object, CacheRefresh> getCacheCallMap() {
        return cacheCallMap;
    }
    public void setRefreshCacheTimer(ScheduledExecutorService refreshCacheTimer) {
        this.refreshCacheTimer = refreshCacheTimer;
    }
    public ScheduledExecutorService getRefreshCacheTimer() {
        return refreshCacheTimer;
    }
    public long currentSeconds() {
        return System.currentTimeMillis() / 1000;
    }
    
    /**
     * 刷新缓存
     * 1、在获取到缓存的时候：如果达到刷新时间则，异步刷新缓存，过期时间前3秒之前才刷新
     * @param key
     */
    public void refreshCache(Object key) {
        if (refreshSecond <= 0) {
            return;
        }
        CacheRefresh cacheRefresh = cacheCallMap.get(key);
        if (null != cacheRefresh && null != refreshCacheTimer) {
            long currentSeconds = currentSeconds();
            long createSeconds = cacheRefresh.getCurrentSeconds();
            /** 缓存已执行的时间 */
            long executeSeconds = currentSeconds - createSeconds;
            /** 大于刷新时间且小于超时时间2秒 */
            int minRefreshSecond = 2;
            if (executeSeconds >= refreshSecond && executeSeconds < expireSecond - minRefreshSecond) {
                /** 是否添加过key */
                if (refreshKeyMap.containsKey(key)) {
                    return;
                }
                refreshCache(key, cacheRefresh);
            }
        }
    }

    private void refreshCache(Object key, CacheRefresh cacheRefresh) {
        ReentrantLock lock = cacheRefresh.lock;
        boolean flag = lock.tryLock();
        /** 控制一个异步刷新缓存 */
        if (flag) {
            refreshKeyMap.put(key, Boolean.TRUE);
            try {
                refreshCacheTimer.schedule(() -> {
                    try {
                        Object obj = cacheRefresh.getValueLoader().call();
                        put(key, obj);
                        saveCacheRefresh(key, cacheRefresh.getValueLoader());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        refreshKeyMap.remove(key);
                    }
                }, 0, TimeUnit.SECONDS);
            } finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * 记录缓存值
     * @param key
     * @param valueLoader
     */
    public void saveCacheRefresh(Object key, Callable<?> valueLoader) {
        if (refreshSecond <= 0 && null != refreshCacheTimer) {
            return;
        }
        // 记录缓存调用的方法
        CacheRefresh cacheRefresh = cacheCallMap.get(key);
        long currentSeconds = currentSeconds();
        if (cacheRefresh == null) {
            cacheRefresh = new CacheRefresh(currentSeconds, valueLoader);
        } else {
            cacheRefresh.setCurrentSeconds(currentSeconds);
        }
        cacheCallMap.put(key, cacheRefresh);
    }
    
    private static class CacheRefresh {
        /** 缓存调用的方法 */
        private Callable<?> valueLoader;
        /** 获取方法的时间 */
        private long currentSeconds;
        /** 控制一个线程执行异步刷新 */
        private ReentrantLock lock = new ReentrantLock();
        
        public CacheRefresh(long currentSeconds, Callable<?> valueLoader) {
            this.currentSeconds = currentSeconds;
            this.valueLoader = valueLoader;
        }
        @SuppressWarnings("rawtypes")
        public Callable getValueLoader() {
            return valueLoader;
        }
        public void setValueLoader(Callable<?> valueLoader) {
            this.valueLoader = valueLoader;
        }
        public long getCurrentSeconds() {
            return currentSeconds;
        }
        public void setCurrentSeconds(long currentSeconds) {
            this.currentSeconds = currentSeconds;
        }
        public ReentrantLock getLock() {
            return lock;
        }
        public void setLock(ReentrantLock lock) {
            this.lock = lock;
        }
    }
}
