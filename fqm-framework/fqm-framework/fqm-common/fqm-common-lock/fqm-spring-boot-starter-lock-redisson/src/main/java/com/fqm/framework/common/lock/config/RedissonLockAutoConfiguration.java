package com.fqm.framework.common.lock.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.lock.LockFactory;
import com.fqm.framework.common.lock.template.RedissonLockTemplate;

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
    
}
