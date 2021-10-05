package com.fqm.framework.sentry.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.fqm.framework.sentry.resolver.DoNothingExceptionResolver;

import io.sentry.NoOpHub;
import io.sentry.spring.SentryExceptionResolver;
import io.sentry.spring.boot.SentryAutoConfiguration;

/**
 * 自定义的 Sentry 自动配置类
 * 
 * @version 
 * @author 傅泉明
 */
@ConditionalOnClass({HandlerExceptionResolver.class, SentryExceptionResolver.class})
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "sentry.enable", havingValue = "true")
@Configuration(proxyBeanMethods = false)
public class CustomSentryAutoConfiguration {

    /**
     * 用于覆盖原有的 SentryStarter 提供的 SentryExceptionResolver 操作
     * 解决使用 log appender 形式推送错误信息与全局异常捕获导致重复推送的情况
     *
     * @return DoNothingExceptionResolver
     */
    @Bean
    @ConditionalOnClass(SentryAutoConfiguration.class)
    @ConditionalOnMissingBean(SentryExceptionResolver.class)
    public SentryExceptionResolver doNothingExceptionResolver() {
        return new DoNothingExceptionResolver(NoOpHub.getInstance(), 0);
    }

}
