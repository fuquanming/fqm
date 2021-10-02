package com.fqm.framework.web.config;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fqm.framework.web.core.handler.GlobalExceptionHandler;
import com.fqm.framework.web.core.handler.GlobalRequestBodyHandler;
import com.fqm.framework.web.core.handler.GlobalResponseBodyHandler;
import com.fqm.framework.web.core.servlet.CorsFilter;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonWebAutoConfiguration implements WebMvcConfigurer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // ========== 全局处理器 ==========

    @Bean
    @ConditionalOnMissingBean(GlobalRequestBodyHandler.class)
    public GlobalRequestBodyHandler globalRequestBodyHandler() {
        return new GlobalRequestBodyHandler();
    }

    @Bean
    @ConditionalOnMissingBean(GlobalResponseBodyHandler.class)
    public GlobalResponseBodyHandler globalResponseBodyHandler() {
        return new GlobalResponseBodyHandler();
    }

    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    // ========== 拦截器相关 ==========

//    @Bean
//    @ConditionalOnClass(name = {"com.fqm.system.rpc.log.SystemExceptionLogRpc"})
//    @ConditionalOnMissingBean(AccessLogInterceptor.class)
//    public AccessLogInterceptor accessLogInterceptor() {
//        return new AccessLogInterceptor();
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        try {
//            registry.addInterceptor(this.accessLogInterceptor());
//            logger.info("[addInterceptors][加载 AccessLogInterceptor 拦截器完成]");
//        } catch (NoSuchBeanDefinitionException e) {
//            logger.warn("[addInterceptors][无法获取 AccessLogInterceptor 拦截器，因此不启动 AccessLog 的记录]");
//        }
//    }

    // ========== 过滤器相关 ==========

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorsFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    // ========== MessageConverter 相关 ==========

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

}
