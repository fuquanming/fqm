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

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.Assert;

import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;

/**
 * 锁校验
 * 1、校验配置文件
 * 2、判断配置文件中是否加载锁组件
 * lock.binder 或者 lock.locks.xxx.binder
 * @version 
 * @author 傅泉明
 */
public class LockVerification implements SmartInitializingSingleton {

    private LockFactory lockFactory;

    private LockProperties lockProperties;

    public LockVerification(LockFactory lockFactory, LockProperties lockProperties) {
        this.lockFactory = lockFactory;
        this.lockProperties = lockProperties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // 1、校验配置文件
        LockMode lockMode = lockProperties.getBinder();
        lockProperties.getLocks().entrySet().forEach(entry -> {
            String businessName = entry.getKey();
            LockConfigurationProperties lcp = entry.getValue();

            LockMode binder = lcp.getBinder();
            if (null == binder) {
                binder = lockMode;
            }
            Assert.notNull(binder, "Please specific [binder] under [lock.locks." + businessName + "] configuration or [binder] under [lock] configuration.");
            // 校验 LockMode 
            Assert.isTrue(lockFactory.containsLockTemplate(binder), "Please specific [binder] under [lock.locks." + businessName + "] configuration, not found [" + binder + "].");
            // 锁的名称
            Assert.hasText(lcp.getKey(), "Please specific [key] under [lock.locks." + businessName + "] configuration.");
        });
    }

}
