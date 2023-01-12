package com.fqm.framework.cache.spring.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;

import com.fqm.framework.common.redisson.RedissonFactory;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@EnableCaching
@Configuration
public class MultilevelCacheManagerRedissonAutoConfiguration extends ApplicationObjectSupport {
    
    /**
     * 使用 redisson-spring-boot-starter 初始化RedissonClient，不用初始化RedissonConfig及RedissonClient，
     * 会自动初始化RedissonClient，使用spring.redis配置或spring.redis.redisson配置
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    RedissonClient redissonClient(RedisProperties redisProperties) {
        return RedissonFactory.getClient(redisProperties);
    }
}
