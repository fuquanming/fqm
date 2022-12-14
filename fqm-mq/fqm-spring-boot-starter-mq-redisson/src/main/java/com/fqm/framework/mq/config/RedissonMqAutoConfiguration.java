package com.fqm.framework.mq.config;

import java.util.ArrayList;
import java.util.List;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fqm.framework.common.redisson.RedissonConfig;
import com.fqm.framework.common.redisson.RedissonFactory;
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
        List<RedissonMqListener> listenerList = new ArrayList<>();
        for (MqListenerParam v : mq.getListeners()) {
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties != null && MqMode.REDISSON.equalMode(properties.getBinder())) {
                String group = properties.getGroup();
                String topic = properties.getTopic();
                Assert.isTrue(StringUtils.hasText(topic), "Please specific [topic] under mq.mqs." + name + " configuration.");
                Assert.isTrue(StringUtils.hasText(group), "Please specific [group] under mq.mqs." + name + " configuration.");
                RedissonMqListener redissonMqListener = new RedissonMqListener(v.getBean(), v.getMethod(), redissonClient, topic);
                listenerList.add(redissonMqListener);
                logger.info("Init RedissonMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
            }
        }
        if (!listenerList.isEmpty()) {
            RedissonMqListenerContainer container = new RedissonMqListenerContainer(listenerList.size());
            listenerList.forEach(container::register);
            return container;
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.redis")
    RedissonConfig redissonProperties() {
        return new RedissonConfig();
    }
    
    @Bean
    @ConditionalOnMissingBean
    RedissonClient redissonClient(RedissonConfig redissonProperties) {
        return RedissonFactory.getClient(redissonProperties);
    }
}
