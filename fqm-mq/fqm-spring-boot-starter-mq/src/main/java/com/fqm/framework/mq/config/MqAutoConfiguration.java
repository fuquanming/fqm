package com.fqm.framework.mq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;

/**
 * 消息队列自动注册
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true")
@EnableConfigurationProperties(MqProperties.class)
public class MqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MqFactory mqFactory() {
        return new MqFactory();
    }

    @Bean
    public MqListenerAnnotationBeanPostProcessor mqListenerAnnotationBeanPostProcessor() {
        return new MqListenerAnnotationBeanPostProcessor();
    }
}
