/*
 * @(#)ElasticJobPropertiesBeanPostProcessor.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job-elastic
 * 创建日期 : 2022年9月12日
 * 修改历史 : 
 *     1. [2022年9月12日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import org.apache.shardingsphere.elasticjob.lite.spring.boot.reg.ZookeeperProperties;
import org.apache.shardingsphere.elasticjob.lite.spring.boot.tracing.TracingProperties;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * ElasticJob配置文件初始化设置默认值
 * 1、Zookeeper的配置
 * connection-timeout-milliseconds: 15000
 * session-timeout-milliseconds: 30000
 * max-retries: 3
 * max-sleep-time-milliseconds: 10000
 * 2、TracingProperties 添加事件跟踪时配置数据库连接，并使用HikariPool连接池，Druid连接池保存会报错，ID冲突
 * 为elasticjob.tracing.type: RDB 时 配置数据源，参见 ElasticJobTracingConfiguration
 * @version 
 * @author 傅泉明
 */
public class ElasticJobPropertiesBeanPostProcessor implements BeanPostProcessor, Ordered, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass == ZookeeperProperties.class) {
            ZookeeperProperties zp = (ZookeeperProperties) bean;
            int connectionTimeoutMilliseconds = 15000;
            int sessionTimeoutMilliseconds = 30000;
            int maxSleepTimeMilliseconds = 10000;
            if (zp.getConnectionTimeoutMilliseconds() < connectionTimeoutMilliseconds) {
                zp.setConnectionTimeoutMilliseconds(connectionTimeoutMilliseconds);
            }
            if (zp.getSessionTimeoutMilliseconds() < sessionTimeoutMilliseconds) {
                zp.setSessionTimeoutMilliseconds(sessionTimeoutMilliseconds);
            }
            if (zp.getMaxSleepTimeMilliseconds() < maxSleepTimeMilliseconds) {
                zp.setMaxSleepTimeMilliseconds(maxSleepTimeMilliseconds);
            }
        } else if (targetClass == TracingProperties.class) {
            TracingProperties tp = (TracingProperties) bean;
            // 强制加载配置
            if (ElasticJobTracingDataSourceProperties.TRACING_TYPE.equals(tp.getType())) {
                ElasticJobTracingDataSourceProperties dataSourceProperties = applicationContext.getBean(ElasticJobTracingDataSourceProperties.class);
                // 手动赋值数据源
                Assert.notNull(dataSourceProperties.getDb(), "Please specific [db] under [elasticjob.tracing] configuration.");
                Assert.notNull(dataSourceProperties.getDb().getUrl(), "Please specific [url] under [elasticjob.tracing.db] configuration.");
                Assert.notNull(dataSourceProperties.getDb().getDriverClassName(), "Please specific [driver-class-name] under [elasticjob.tracing.db] configuration.");
                Assert.notNull(dataSourceProperties.getDb().getUsername(), "Please specific [username] under [elasticjob.tracing.db] configuration.");
                Assert.notNull(dataSourceProperties.getDb().getPassword(), "Please specific [password] under [elasticjob.tracing.db] configuration.");
                
                DataSourceProperties dsp = new DataSourceProperties();
                dsp.setUrl(dataSourceProperties.getDb().getUrl());
                dsp.setDriverClassName(dataSourceProperties.getDb().getDriverClassName());
                dsp.setUsername(dataSourceProperties.getDb().getUsername());
                dsp.setPassword(dataSourceProperties.getDb().getPassword());
                tp.setDataSource(dsp);
            }
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
