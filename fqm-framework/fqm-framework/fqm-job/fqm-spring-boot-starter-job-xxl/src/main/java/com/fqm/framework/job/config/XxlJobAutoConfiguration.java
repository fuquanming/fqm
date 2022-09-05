/*
 * @(#)XxlJobAutoConfiguration.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job-xxl
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.job.JobMode;
import com.fqm.framework.job.annotation.JobListenerAnnotationBeanPostProcessor;
import com.fqm.framework.job.listener.JobListenerParam;
import com.fqm.framework.job.listener.XxlJobListener;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;

/**
 * xxl-job 自动配置类
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnClass(XxlJobSpringExecutor.class)
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({XxlJobConfig.class})
public class XxlJobAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(XxlJobAutoConfiguration.class);

    private final XxlJobConfig properties;

    public XxlJobAutoConfiguration(XxlJobConfig properties) {
        this.properties = properties;
    }

    @Bean(initMethod = "start", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public XxlJobExecutor xxlJobExecutor() {
        LOGGER.info("初始化 XXL-Job 执行器的配置");

        // 参数校验
        XxlJobConfig.AdminProperties admin = this.properties.getAdmin();
        XxlJobConfig.ExecutorProperties executor = this.properties.getExecutor();
        Objects.requireNonNull(admin, "xxl job admin properties must not be null.");
        Objects.requireNonNull(executor, "xxl job executor properties must not be null.");

        // 初始化执行器
        XxlJobExecutor xxlJobExecutor = new XxlJobExecutor();
        xxlJobExecutor.setIp(executor.getIp());
        xxlJobExecutor.setPort(executor.getPort());
        xxlJobExecutor.setAppname(executor.getAppName());
        xxlJobExecutor.setLogPath(executor.getLogPath());
        xxlJobExecutor.setLogRetentionDays(executor.getLogRetentionDays());
        xxlJobExecutor.setAdminAddresses(admin.getAddresses());
        xxlJobExecutor.setAccessToken(this.properties.getAccessToken());
        return xxlJobExecutor;
    }
    
    @Resource
    JobListenerAnnotationBeanPostProcessor job;
    
    @PostConstruct
    public void init() {
        for (JobListenerParam v : job.getListeners()) {
            if (JobMode.xxl.name().equals(v.getBinder())) {
                new XxlJobListener(v.getBean(), v.getMethod(), v.getName());
            }
        }
    }
    
}
