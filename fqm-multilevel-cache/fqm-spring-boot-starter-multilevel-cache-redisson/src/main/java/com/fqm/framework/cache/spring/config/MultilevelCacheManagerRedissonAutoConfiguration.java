package com.fqm.framework.cache.spring.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;

import com.fqm.framework.common.redisson.RedissonConfig;
import com.fqm.framework.common.redisson.RedissonFactory;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@EnableCaching
@Configuration
public class MultilevelCacheManagerRedissonAutoConfiguration extends ApplicationObjectSupport {
    
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.redis")
    public RedissonConfig redissonProperties() {
        return new RedissonConfig();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedissonConfig redissonProperties) {
        return RedissonFactory.getClient(redissonProperties);
    }
}
