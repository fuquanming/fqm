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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.job.annotation.JobListenerAnnotationBeanPostProcessor;

/**
 * 任务自动注册
 * @version 
 * @author 傅泉明
 */
@Configuration
public class JobAutoConfiguration {

    @Bean
    public JobListenerAnnotationBeanPostProcessor jobListenerAnnotationBeanPostProcessor() {
        return new JobListenerAnnotationBeanPostProcessor();
    }
    
}
