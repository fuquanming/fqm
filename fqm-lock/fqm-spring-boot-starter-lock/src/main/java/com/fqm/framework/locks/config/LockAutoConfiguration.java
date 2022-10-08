package com.fqm.framework.locks.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.aop.LockAnnotationAdvisor;
import com.fqm.framework.locks.aop.LockInterceptor;

/**
 *
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class LockAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public LockFactory lockFactory() {
        LockFactory lockFactory = new LockFactory();
        return lockFactory;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public LockInterceptor lockInterceptor(LockFactory lockFactory, ApplicationContext applicationContext) {
        return new LockInterceptor(lockFactory, applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public LockAnnotationAdvisor lockAnnotationAdvisor(LockInterceptor lockInterceptor) {
        return new LockAnnotationAdvisor(lockInterceptor, Ordered.HIGHEST_PRECEDENCE);
    }
    
}
