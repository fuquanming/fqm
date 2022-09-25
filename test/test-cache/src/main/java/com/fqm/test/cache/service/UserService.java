package com.fqm.test.cache.service;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Cacheable(value = "user|${cache.user.expire}|${cache.user.nullExpire}|${cache.user.refresh}", key = "'cache_user_id_' + #id", sync = true
    //          @Cacheable(value = "user|110|90", key = "'cache_user_id_' + #id", sync = true
    //                  , cacheManager = "multiLevelCacheRedis"
    )
    //          @Cacheable(value = "CAFFEINE", key = "'cache_user_id_' + #id", sync = true, cacheManager = "caffeineCacheManager")
    public HashMap<String, Object> getCacheById(Long id) {
        logger.info("load db...getCacheById");
        return new HashMap<>();
    }
}
