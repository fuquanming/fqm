/*
 * @(#)LockModeVerification.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-lock
 * 创建日期 : 2023年3月2日
 * 修改历史 : 
 *     1. [2023年3月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.locks.config;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;

/**
 * 判断配置文件中是否加载锁组件
 * lock.binder 或者 lock.locks.xxx.binder
 * @version 
 * @author 傅泉明
 */
public class LockModeVerification implements ApplicationRunner, ApplicationContextAware {

    /**
     * Spring应用上下文环境
     */
    private ApplicationContext applicationContext;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LockFactory lockFactory = applicationContext.getBean(LockFactory.class);
        LockProperties lockProperties = applicationContext.getBean(LockProperties.class);
        LockMode lockMode = lockProperties.getBinder();
        if (null != lockMode) {
            Assert.isTrue(null != lockFactory.getLockTemplate(lockMode), "Please specific [binder] under [lock.binder] configuration, not found [" + lockMode + "]");
        }
        Map<String, LockConfigurationProperties> locks = lockProperties.getLocks();
        locks.entrySet().forEach(entry -> {
            String name = entry.getKey();
            LockConfigurationProperties lcp = entry.getValue();
            LockMode binder = lcp.getBinder();
            if (null != binder) {
                Assert.isTrue(null != lockFactory.getLockTemplate(binder), "Please specific [binder] under [lock.locks." + name + "] configuration, not found [" + binder + "]");
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
