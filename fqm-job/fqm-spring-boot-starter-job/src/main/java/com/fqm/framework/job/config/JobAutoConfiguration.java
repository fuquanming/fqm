/*
 * @(#)JobAutoConfiguration.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.job.annotation.JobListenerAnnotationBeanPostProcessor;

/**
 * 任务自动装配
 * @ConditionalOnProperty true 开启，默认值为false
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnProperty(name = "job.enabled", havingValue = "true")
public class JobAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "job")
    JobProperties jobProperties() {
        return new JobProperties();
    }
    
    @Bean
    JobListenerAnnotationBeanPostProcessor jobListenerAnnotationBeanPostProcessor() {
        return new JobListenerAnnotationBeanPostProcessor();
    }
    
    @Bean
    @ConditionalOnProperty(name = "job.verify", havingValue = "true")
    JobVerification jobVerification(JobProperties jobProperties) {
        return new JobVerification(jobProperties);
        
    }
    
}
