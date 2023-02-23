package com.fqm.framework.mq.config;

import java.util.ArrayList;
import java.util.List;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.redisson.RedissonFactory;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
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
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class RedissonMqAutoConfiguration {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    RedissonMqTemplate redissonMqTemplate(MqFactory mqFactory, RedissonClient redissonClient) {
        RedissonMqTemplate redissonMqTemplate = new RedissonMqTemplate(redissonClient);
        mqFactory.addMqTemplate(redissonMqTemplate);
        return redissonMqTemplate;
    }

    @Bean(destroyMethod = "stop")
    RedissonMqListenerContainer redissonMqListener(MqListenerAnnotationBeanPostProcessor mq, RedissonClient redissonClient,
            MqProperties mp) {
        List<MqListenerParam> listenerParams = mq.getListeners(MqMode.REDISSON);
        if (null == listenerParams || listenerParams.isEmpty()) {
            return null;
        }
        
        List<RedissonMqListener> listenerList = new ArrayList<>();
        for (MqListenerParam v : listenerParams) {
            MqListener mqListener = v.getMqListener();
            // 1、解析@MqListener
            String topic = mqListener.topic();
            String group = mqListener.group();
            RedissonMqListener redissonMqListener = new RedissonMqListener(v.getBean(), v.getMethod(), redissonClient, topic);
            listenerList.add(redissonMqListener);
            logger.info("Init RedissonMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
        }
        if (!listenerList.isEmpty()) {
            RedissonMqListenerContainer container = new RedissonMqListenerContainer(listenerList.size());
            listenerList.forEach(container::register);
            return container;
        }
        return null;
    }

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
