package com.fqm.framework.mq.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.redisson.RedissonFactory;
import com.fqm.framework.common.redisson.RedissonConfig;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.RedissonMqListener;
import com.fqm.framework.mq.listener.RedissonMqListenerContainer;
import com.fqm.framework.mq.template.RedissonMqTemplate;

/**
 * Redisson消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class RedissonMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public RedissonMqTemplate redissonMqTemplate(MqFactory mqFactory, RedissonClient redissonClient) {
        RedissonMqTemplate redissonMqTemplate = new RedissonMqTemplate(redissonClient);
        mqFactory.addMqTemplate(redissonMqTemplate);
        return redissonMqTemplate;
    }

    @Bean(destroyMethod = "stop")
    public RedissonMqListenerContainer redissonMqListener(MqListenerAnnotationBeanPostProcessor mq, RedissonClient redissonClient) {
        RedissonMqListenerContainer container = new RedissonMqListenerContainer();
        for (MqListenerParam v : mq.getListeners()) {
            if (MqMode.redisson.name().equals(v.getBinder())) {
                RedissonMqListener redissonMqListener = new RedissonMqListener(v.getBean(), v.getMethod(), redissonClient, v.getTopic(),
                        v.getGroup());
                container.register(redissonMqListener);
            }
        }
        return container;
    }

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
