package com.fqm.framework.common.redis.listener.spring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.context.event.EventListener;

import com.fqm.framework.cache.spring.MultilevelCacheManager;

/**
 * 监听Redis 删除key事件 -> 删除本地缓存
 * 
 * @version 
 * @author 傅泉明
 */
public class CacheRedisKeyDeleteEventHandle {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // user|12|9|::cache_user_id_89
    Pattern p = Pattern.compile("(.*)::(.*)");
    
    private MultilevelCacheManager cacheManager;
    
    public CacheRedisKeyDeleteEventHandle(MultilevelCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventListener
    public void eventHandle(RedisKeyDeleteEvent event) {
        String deleteKey = new String(event.getSource());
//        logger.info("RedisKeyDelete=" + deleteKey);
        
        Matcher m = p.matcher(deleteKey);
        if (m.find()) {
            Cache cache = cacheManager.getCacheMap().get(m.group(1));
            if (cache != null) {
                String cacheKey = m.group(2);
                // 删除缓存
                cache.evict(cacheKey);
                logger.info("MultiLevelCache Delete cacheKey=" + cacheKey);
            }
        }
    }
    
}
