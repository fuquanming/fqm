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

import java.util.List;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.spring.boot.tracing.TracingProperties;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fqm.framework.job.JobMode;
import com.fqm.framework.job.annotation.JobListenerAnnotationBeanPostProcessor;
import com.fqm.framework.job.listener.ElasticJobListener;
import com.fqm.framework.job.listener.JobListenerParam;

/**
 * ElasticJob 自动配置类
 * JobProperties加载，并在JobAutoConfiguration后加载
 * ElasticJobLiteAutoConfiguration
 * SmartInitializingSingleton接口在Bean加载完成后，加载ElasticJob
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(JobAutoConfiguration.class)
@ConditionalOnBean(JobProperties.class)
@Import(ElasticJobPropertiesBeanPostProcessor.class)
public class ElasticJobAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        JobProperties.JOB_MODE_MAP.add(JobMode.ELASTICJOB);
    }    
    
    @Bean
    @ConfigurationProperties(prefix = "elasticjob.tracing")
    ElasticJobTracingDataSourceProperties elasticJobTracingDataSourceProperties() {
        return new ElasticJobTracingDataSourceProperties();
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        JobListenerAnnotationBeanPostProcessor job = applicationContext.getBean(JobListenerAnnotationBeanPostProcessor.class);
        List<JobListenerParam> listenerParams = job.getListeners(JobMode.ELASTICJOB);
        if (null == listenerParams || listenerParams.isEmpty()) {
            return;
        }
        SingletonBeanRegistry singletonBeanRegistry = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        CoordinatorRegistryCenter registryCenter = applicationContext.getBean(CoordinatorRegistryCenter.class);
        JobProperties jp = applicationContext.getBean(JobProperties.class);
        
        // 注册 监听任务的客户端
        for (JobListenerParam v : listenerParams) {
            String jobName = v.getName();
            JobConfigurationProperties properties = jp.getJobs().get(jobName);
            String cron = properties.getCron();
            Assert.isTrue(StringUtils.hasText(cron), "Please specific [cron] under job.jobs." + jobName + " configuration.");
            buildJob(singletonBeanRegistry, registryCenter, v, jobName, properties);
        }
    }

    private void buildJob(SingletonBeanRegistry singletonBeanRegistry, CoordinatorRegistryCenter registryCenter, JobListenerParam v, String jobName,
            JobConfigurationProperties properties) {
        if (!singletonBeanRegistry.containsSingleton(jobName)) {
            String cron = properties.getCron();
            // overwrite=false，本地配置不覆盖 zk 里的配置
            JobConfiguration jobConfig = JobConfiguration.newBuilder(jobName, 1).cron(cron).overwrite(false)
                    .build();
            // 获取事件跟踪配置，ElasticJobTracingConfiguration，配置elasticjob.tracing.type: RDB，默认加载bean中为 DataSource 作为数据源，并初始化表结构，ElasticJobPropertiesBeanPostProcessor
            // 需要应用有数据库连接池(HikariCP)、数据库驱动(mysql)、数据库JDBC(spring-boot-starter-jdbc)的依赖
            TracingProperties tp = applicationContext.getBean(TracingProperties.class);
            boolean tracingFlag = false;
            if (ElasticJobTracingDataSourceProperties.TRACING_TYPE.equals(tp.getType())) {
                TracingConfiguration<?> tracingConfig = applicationContext.getBean(TracingConfiguration.class);
                jobConfig.getExtraConfigurations().add(tracingConfig);
                tracingFlag = true;
            }
            ElasticJob elasticJob = new ElasticJobListener(v.getBean(), v.getMethod());
            //将applicationContext转换为ConfigurableApplicationContext
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            // 获取bean工厂并转换为DefaultListableBeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            if (StringUtils.hasText(cron)) {
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ScheduleJobBootstrap.class);
                beanDefinitionBuilder.addConstructorArgValue(registryCenter);
                beanDefinitionBuilder.addConstructorArgValue(elasticJob);
                beanDefinitionBuilder.addConstructorArgValue(jobConfig);
                beanDefinitionBuilder.setInitMethodName("schedule");
                beanDefinitionBuilder.setDestroyMethodName("shutdown");
                defaultListableBeanFactory.registerBeanDefinition(jobName, beanDefinitionBuilder.getRawBeanDefinition());
            } else {
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(OneOffJobBootstrap.class);
                beanDefinitionBuilder.addConstructorArgValue(registryCenter);
                beanDefinitionBuilder.addConstructorArgValue(elasticJob);
                beanDefinitionBuilder.addConstructorArgValue(jobConfig);
                beanDefinitionBuilder.setInitMethodName("execute");
                beanDefinitionBuilder.setDestroyMethodName("shutdown");
                defaultListableBeanFactory.registerBeanDefinition(jobName, beanDefinitionBuilder.getRawBeanDefinition());
            }
            logger.info("InitJob ElasticJobListener,bean={},method={},tracing={}", v.getBean().getClass(), v.getMethod().getName(), tracingFlag);
        }
    }
    
}
