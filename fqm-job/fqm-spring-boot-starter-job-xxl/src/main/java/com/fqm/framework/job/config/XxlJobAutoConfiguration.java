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

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import com.fqm.framework.job.JobMode;
import com.fqm.framework.job.annotation.JobListenerAnnotationBeanPostProcessor;
import com.fqm.framework.job.config.XxlJobProperties.AdminProperties;
import com.fqm.framework.job.config.XxlJobProperties.ExecutorProperties;
import com.fqm.framework.job.core.JobContext;
import com.fqm.framework.job.listener.JobListenerParam;
import com.fqm.framework.job.listener.XxlJobListener;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.impl.MethodJobHandler;

/**
 * xxl-job 自动配置类
 * JobProperties加载则JobAutoConfiguration也就加载
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(JobAutoConfiguration.class)
@ConditionalOnBean(JobProperties.class)
@EnableConfigurationProperties({ XxlJobProperties.class })
public class XxlJobAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean(initMethod = "start", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public XxlJobExecutor xxlJobExecutor(XxlJobProperties properties) {
        logger.info("初始化 XXL-Job 执行器的配置");
        // 参数校验
        AdminProperties admin = properties.getAdmin();
        ExecutorProperties executor = properties.getExecutor();
        Assert.isTrue(admin != null, "Please specific [admin] under xxljob configuration.");
        Assert.isTrue(executor != null, "Please specific [executor] under xxljob configuration.");

        // 初始化执行器
        XxlJobExecutor xxlJobExecutor = new XxlJobExecutor();
        xxlJobExecutor.setIp(executor.getIp());
        xxlJobExecutor.setPort(executor.getPort());
        xxlJobExecutor.setAppname(executor.getAppName());
        xxlJobExecutor.setLogPath(executor.getLogPath());
        xxlJobExecutor.setLogRetentionDays(executor.getLogRetentionDays());
        xxlJobExecutor.setAdminAddresses(admin.getAddresses());
        xxlJobExecutor.setAccessToken(properties.getAccessToken());
        return xxlJobExecutor;
    }

    @Override
    public void afterSingletonsInstantiated() {
        JobListenerAnnotationBeanPostProcessor job = applicationContext.getBean(JobListenerAnnotationBeanPostProcessor.class);
        JobProperties jp = applicationContext.getBean(JobProperties.class);
        for (JobListenerParam v : job.getListeners()) {
            String jobName = v.getName();
            JobConfigurationProperties properties = jp.getJobs().get(jobName);
            if (properties == null) {
                // 遍历jp.Jobs
                for (JobConfigurationProperties jcp : jp.getJobs().values()) {
                    if (jcp.getName().equals(jobName) && JobMode.XXLJOB.name().equalsIgnoreCase(jcp.getBinder())) {
                        properties = jcp;
                        break;
                    }
                }

            }
            buildJob(v, jobName, properties);
        }
    }

    private void buildJob(JobListenerParam v, String jobName, JobConfigurationProperties properties) {
        if (properties != null && JobMode.XXLJOB.name().equalsIgnoreCase(properties.getBinder()) && XxlJobExecutor.loadJobHandler(jobName) == null) {
            XxlJobListener listener = new XxlJobListener(v.getBean(), v.getMethod());
            // 获取任务执行的方法
            Method[] ms = listener.getClass().getMethods();
            Method jobMethod = null;
            for (Method m : ms) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 1 && params[0] == JobContext.class) {
                    jobMethod = m;
                    break;
                }
            }
            // 注册本身的 receiveJob 方法,jobName 即管理控台->任务管理->运行模式:(BEAN:名称)
            XxlJobExecutor.registJobHandler(jobName, new MethodJobHandler(listener, jobMethod, null, null));
            logger.info("InitJob XxlJobListener,bean={},method={}", v.getBean().getClass(), v.getMethod().getName());
        }
    }

}
