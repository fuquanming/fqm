package com.fqm.framework.locks.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.annotation.Lock4jAnnotationBeanPostProcessor;
import com.fqm.framework.locks.aop.LockAnnotationAdvisor;
import com.fqm.framework.locks.aop.LockInterceptor;

/**
 *
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnProperty(name = "lock.enabled", havingValue = "true")
public class LockAutoConfiguration {
    
    /**
     * 使用 @Bean 注入 则beanName=lockProperties，否则beanName=lock-com.fqm.framework.locks.config.LockProperties
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "lock")
    LockProperties lockProperties() {
        return new LockProperties();
    }
    
    @Bean
    @ConditionalOnMissingBean
    LockFactory lockFactory() {
        return new LockFactory();
    }
    
    @Bean
    LockProducer lockProducer(LockFactory lockFactory, LockProperties lockProperties) {
        return new LockProducer(lockFactory, lockProperties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    LockInterceptor lockInterceptor(LockFactory lockFactory, LockProducer lockProducer, ApplicationContext applicationContext) {
        return new LockInterceptor(lockFactory, lockProducer, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    LockAnnotationAdvisor lockAnnotationAdvisor(LockInterceptor lockInterceptor) {
        return new LockAnnotationAdvisor(lockInterceptor, Ordered.HIGHEST_PRECEDENCE);
    }
    
    @Bean
    Lock4jAnnotationBeanPostProcessor lock4jAnnotationBeanPostProcessor(LockProperties lockProperties) {
        return new Lock4jAnnotationBeanPostProcessor(lockProperties);
    }
    
//    @Bean
//    @ConditionalOnProperty(name = "lock.verify", havingValue = "true")
//    LockModeVerification lockModeVerification() {
//        return new LockModeVerification();
//    }
}
