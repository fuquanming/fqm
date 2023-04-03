package com.fqm.framework.locks.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.annotation.Lock4jAnnotationBeanPostProcessor;
import com.fqm.framework.locks.aop.LockAnnotationAdvisor;
import com.fqm.framework.locks.aop.LockInterceptor;

/**
 *
 * 分布式锁自动注册
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

    /**
     * stataic 装饰：先LockAutoConfiguration初始化，LockAnnotationAdvisor 是Adviso 拦截器
     * @param applicationContext
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    static LockAnnotationAdvisor lockAnnotationAdvisor(ApplicationContext applicationContext) {
        return new LockAnnotationAdvisor(new LockInterceptor(applicationContext));
    }

    /**
     * stataic 装饰：先LockAutoConfiguration初始化，Lock4jAnnotationBeanPostProcessor 是BeanPostProcessor 拦截器
     * @return
     */
    @Bean
    static Lock4jAnnotationBeanPostProcessor lock4jAnnotationBeanPostProcessor() {
        return new Lock4jAnnotationBeanPostProcessor();
    }

    @Bean
    @ConditionalOnProperty(name = "lock.verify", havingValue = "true")
    LockVerification lockVerification(LockFactory lockFactory, LockProperties lockProperties) {
        return new LockVerification(lockFactory, lockProperties);
    }
    
}
