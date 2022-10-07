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
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * ElasticJob配置文件初始化设置默认值
 * 1、Zookeeper的配置
 * connection-timeout-milliseconds: 15000
 * session-timeout-milliseconds: 30000
 * max-retries: 3
 * max-sleep-time-milliseconds: 10000
 * @version 
 * @author 傅泉明
 */
public class ElasticJobPropertiesBeanPostProcessor implements BeanPostProcessor, Ordered {

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
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
