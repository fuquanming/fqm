package com.fqm.framework.mq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
public class MqAutoConfiguration {

    /**
     * 使用 @Bean 注入 则beanName=mqProperties，否则beanName=mq-com.fqm.framework.mq.config.MqProperties
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "mq")
    MqProperties mqProperties() {
        return new MqProperties();
    }
    
    @Bean
    @ConditionalOnMissingBean
    MqFactory mqFactory() {
        return new MqFactory();
    }
    
    @Bean
    MqProducer mqProducer(MqFactory mqFactory, MqProperties mqProperties) {
        return new MqProducer(mqFactory, mqProperties);
    }

    @Bean
    MqListenerAnnotationBeanPostProcessor mqListenerAnnotationBeanPostProcessor(MqProperties mqProperties) {
        return new MqListenerAnnotationBeanPostProcessor(mqProperties);
    }
}
