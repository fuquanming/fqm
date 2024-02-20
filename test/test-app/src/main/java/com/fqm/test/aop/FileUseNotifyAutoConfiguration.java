package com.fqm.test.aop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 *
 * 分布式锁自动注册
 * @version 
 * @author 傅泉明
 */
@Configuration
@EnableAsync
//@ConditionalOnProperty(name = "lock.enabled", havingValue = "true")
public class FileUseNotifyAutoConfiguration {

    /**
     * stataic 装饰：先LockAutoConfiguration初始化，LockAnnotationAdvisor 是Adviso 拦截器
     * @param applicationContext
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    static FileUseNotifyAnnotationAdvisor fileUseNotifyAnnotationAdvisor(
            ApplicationContext applicationContext, ApplicationEventPublisher publisher) {
        return new FileUseNotifyAnnotationAdvisor(new FileUseNotifyInterceptor(applicationContext, publisher));
    }

}
