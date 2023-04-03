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

    /**
     * stataic 装饰：先MqAutoConfiguration初始化，MqListenerAnnotationBeanPostProcessor 是BeanPostProcessor 拦截器
     * @return
     */
    @Bean
    static MqListenerAnnotationBeanPostProcessor mqListenerAnnotationBeanPostProcessor() {
        return new MqListenerAnnotationBeanPostProcessor();
    }
    
    @Bean
    @ConditionalOnProperty(name = "mq.verify", havingValue = "true")
    MqVerification mqVerification(MqFactory mqFactory, MqProperties mqProperties) {
        return new MqVerification(mqFactory, mqProperties);
    }
}
