/*
 * @(#)ElasticJobAutoConfiguration.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job-elastic
 * 创建日期 : 2022年9月5日
 * 修改历史 : 
 *     1. [2022年9月5日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fqm.framework.common.core.util.StringUtil;
import com.fqm.framework.job.JobMode;
import com.fqm.framework.job.annotation.JobListenerAnnotationBeanPostProcessor;
import com.fqm.framework.job.listener.ElasticJobListener;
import com.fqm.framework.job.listener.JobListenerParam;
import com.google.common.base.Preconditions;

/**
 * ElasticJob自动配置类
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(JobAutoConfiguration.class)
@Import(ElasticJobPropertiesBeanPostProcessor.class)
@ConditionalOnBean(JobProperties.class) // JobProperties加载则JobAutoConfiguration也就加载
public class ElasticJobAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }    

    @Override
    public void afterSingletonsInstantiated() {
        JobListenerAnnotationBeanPostProcessor job = applicationContext.getBean(JobListenerAnnotationBeanPostProcessor.class);
        SingletonBeanRegistry singletonBeanRegistry = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        CoordinatorRegistryCenter registryCenter = applicationContext.getBean(CoordinatorRegistryCenter.class);

        JobProperties jp = applicationContext.getBean(JobProperties.class);

        for (JobListenerParam v : job.getListeners()) {
            String jobName = v.getName();
            JobConfigurationProperties properties = jp.getJobs().get(jobName);
            if (properties == null) {
                // 遍历jp.Jobs
                for (JobConfigurationProperties jcp : jp.getJobs().values()) {
                    if (jcp.getName().equals(jobName) && JobMode.elasticjob.name().equals(jcp.getBinder())) {
                        properties = jcp;
                        break;
                    }
                }

            }
            if (properties != null && JobMode.elasticjob.name().equals(properties.getBinder()) && !singletonBeanRegistry.containsSingleton(jobName)) {
                String cron = properties.getCron();
                Preconditions.checkArgument(StringUtil.isNotBlank(cron), "Please specific [core] under job configuration, binder is elasticjob.");
                JobConfiguration jobConfig = JobConfiguration.newBuilder(jobName, 1).cron(cron).overwrite(false).build();
                ElasticJob elasticJob = new ElasticJobListener(v.getBean(), v.getMethod());
                if (StringUtil.isBlank(cron)) {// cron为空
                    singletonBeanRegistry.registerSingleton(jobName, new OneOffJobBootstrap(registryCenter, elasticJob, jobConfig));
                } else {
                    singletonBeanRegistry.registerSingleton(jobName, new ScheduleJobBootstrap(registryCenter, elasticJob, jobConfig));
                }
                logger.info("InitJob ElasticJobListener,bean={},method={}", v.getBean().getClass(), v.getMethod().getName());
            }
        }
    }
}
