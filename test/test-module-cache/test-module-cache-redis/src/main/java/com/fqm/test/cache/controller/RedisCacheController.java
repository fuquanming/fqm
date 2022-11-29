package com.fqm.test.cache.controller;

import java.util.HashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.thread.ThreadUtil;

@RestController
public class RedisCacheController {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    RedisCacheUserService cacheUserService;
    
    @GetMapping("/cache/redis")
    @ResponseBody
    public Object getCache(
            @RequestParam(name = "id", defaultValue = "100", required = false) Long id) {            
        ThreadUtil.concurrencyTest(3, new Runnable() {
            @Override
            public void run() {
                cacheUserService.getCacheById(id);
            }
        });        
        return cacheUserService.getCacheById(id);
    }
    
    @Service
    class RedisCacheUserService {
        @Cacheable(value = "user|${cache.redis.expire}|${cache.redis.nullExpire}|${cache.redis.refresh}", key = "'cache_user_id_' + #id", sync = true
        //          @Cacheable(value = "user|110|90", key = "'cache_user_id_' + #id", sync = true
        //                  , cacheManager = CacheManagerType.MULTI_LEVEL_CACHE_MANAGER_REDIS
        )
        //          @Cacheable(value = "CAFFEINE", key = "'cache_user_id_' + #id", sync = true, cacheManager = "caffeineCacheManager")
        public HashMap<String, Object> getCacheById(Long id) {
            logger.info("load db...getCacheById");
            return new HashMap<>();
        }
    }
}
