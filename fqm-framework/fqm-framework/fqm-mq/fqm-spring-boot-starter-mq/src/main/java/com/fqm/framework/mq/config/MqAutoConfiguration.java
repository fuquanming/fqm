package com.fqm.framework.mq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class MqAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MqFactory mqFactory() {
        MqFactory mqFactory = new MqFactory();
        return mqFactory;
    }
    
    @Bean
    public MqListenerAnnotationBeanPostProcessor mqListenerAnnotationBeanPostProcessor() {
        return new MqListenerAnnotationBeanPostProcessor();
    }
}
