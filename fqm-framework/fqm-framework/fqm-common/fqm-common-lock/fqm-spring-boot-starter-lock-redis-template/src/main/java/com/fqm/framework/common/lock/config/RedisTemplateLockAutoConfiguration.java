package com.fqm.framework.common.lock.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.fqm.framework.common.lock.LockFactory;
import com.fqm.framework.common.lock.redis.listener.spring.LockRedisKeyDeleteEventHandle;
import com.fqm.framework.common.lock.template.RedisTemplateLockTemplate;
import com.fqm.framework.common.redis.listener.spring.KeyDeleteEventMessageListener;

/**
 * RedisTemplate 锁自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class RedisTemplateLockAutoConfiguration {
    
    @Bean(destroyMethod = "destory")
    @ConditionalOnMissingBean
    @Order(300)
    public RedisTemplateLockTemplate redisTemplateLockTemplate(
            LockFactory lockFactory,
            StringRedisTemplate stringRedisTemplate) {
        RedisTemplateLockTemplate redisTemplateLockTemplate = new RedisTemplateLockTemplate(stringRedisTemplate, lockFactory);
        lockFactory.addLockTemplate(redisTemplateLockTemplate);
        return redisTemplateLockTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(value = RedisMessageListenerContainer.class)
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
    
    /**
     * 监听Redis删除key事件
     * @param listenerContainer
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = KeyDeleteEventMessageListener.class)
    KeyDeleteEventMessageListener keyDeleteEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        return new KeyDeleteEventMessageListener(listenerContainer);
    }
    
    /**
     * 锁处理 监听Redis删除key事件
     * @param listenerContainer
     * @param cacheManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = LockRedisKeyDeleteEventHandle.class)
    LockRedisKeyDeleteEventHandle lockRedisKeyDeleteEventHandle() {
        return new LockRedisKeyDeleteEventHandle();
    }
}
