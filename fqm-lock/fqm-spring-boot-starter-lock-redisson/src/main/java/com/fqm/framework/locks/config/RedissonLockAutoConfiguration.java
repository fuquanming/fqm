package com.fqm.framework.locks.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.redisson.RedissonConfig;
import com.fqm.framework.common.redisson.RedissonFactory;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.template.RedissonLockTemplate;

/**
 * Redisson 锁自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class RedissonLockAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public RedissonLockTemplate redissonLockTemplate(
            LockFactory lockFactory,
            RedissonClient redissonClient) {
        RedissonLockTemplate redissonLockTemplate = new RedissonLockTemplate(redissonClient);
        lockFactory.addLockTemplate(redissonLockTemplate);
        return redissonLockTemplate;
    }
    /**
     * 使用 redisson-spring-boot-starter 初始化RedissonClient，不用初始化RedissonConfig及RedissonClient，
     * 会自动初始化RedissonClient，使用spring.redis配置或spring.redis.redisson配置
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.redis")
    public RedissonConfig redissonConfig() {
        return new RedissonConfig();
    }
    
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedissonConfig redissonConfig) {
        return RedissonFactory.getClient(redissonConfig);
    }
}
