/*
 * @(#)LockProducer.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-lock
 * 创建日期 : 2023年3月1日
 * 修改历史 : 
 *     1. [2023年3月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.locks.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;

import com.fqm.framework.common.core.exception.ErrorCode;
import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;

/**
 * 锁的生产者，通过锁名称获取锁
 * @version 
 * @author 傅泉明
 */
public class LockProducer {

    /** 锁工厂 */
    private LockFactory lockFactory;
    /** 锁配置 */
    private LockProperties lockProperties;
    /** 锁生产者，key：锁名称 */
    private Map<String, Producer> producerMap;
    
    public LockProducer(LockFactory mqFactory, LockProperties lockProperties) {
        this.lockFactory = mqFactory;
        this.lockProperties = lockProperties;
        producerMap = new ConcurrentHashMap<>(lockProperties.getLocks().size());
        init();
    }
    /** 初始化 */
    private void init() {
        LockMode lockMode = lockProperties.getBinder();
        // 1、初始化业务对应的消息生产者
        for (Map.Entry<String, LockConfigurationProperties> entry : lockProperties.getLocks().entrySet()) {
            String businessName = entry.getKey();
            LockConfigurationProperties mcp = entry.getValue();
            LockMode binder = mcp.getBinder();
            if (null == binder) {
                binder = lockMode;
            }
            Assert.isTrue(null != binder, "Please specific [binder] under [lock.locks." + businessName + "] configuration or [binder] under [lock] configuration.");
            Assert.hasText(mcp.getKey(), "Please specific [key] under [lock.locks." + businessName + "] configuration.");
            
            Producer producer = new Producer();
            producer.key = mcp.getKey();
            producer.lockMode = binder;
            producer.acquireTimeout = mcp.getAcquireTimeout();
            producer.block = mcp.isBlock();
            producerMap.put(businessName, producer);
        }
    }
    
    private LockConfigurationProperties getLockConfigurationProperties(String businessName) {
        LockConfigurationProperties lockConfigurationProperties = lockProperties.getLocks().get(businessName);
        if (null == lockConfigurationProperties) {
            throw new ServiceException(new ErrorCode(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未配置该锁的通道"));
        }
        return lockConfigurationProperties;
    }
    
    /**
     * 获取锁组件
     * @param businessName 业务名称
     * @return
     */
    public LockMode getBinder(String businessName) {
        return getLockConfigurationProperties(businessName).getBinder();
    }
    
    /**
     * 获取锁名称
     * @param businessName 业务名称
     * @return
     */
    public String getKey(String businessName) {
        return getLockConfigurationProperties(businessName).getKey();
    }
    
    /**
     * 获取锁生产者
     * @param businessName  业务名称
     * @return
     */
    public Producer getProducer(String businessName) {
        Producer producer = producerMap.get(businessName);
        if (null == producer) {
            throw new ServiceException(new ErrorCode(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未找到该业务的锁，" + businessName));
        }
        return producer;
    }
    
    /**
     * 获取锁生产者
     * @param businessName  业务名称
     * @return
     */
    public Lock getLock(String businessName) {
        Producer producer = producerMap.get(businessName);
        if (null == producer) {
            throw new ServiceException(new ErrorCode(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未找到该业务的锁，" + businessName));
        }
        return producer.getLock();
    }
    /**
     * 锁生产者 
     * @version 
     * @author 傅泉明
     */
    public class Producer {
        LockMode lockMode;
        String key;
        boolean block;
        long acquireTimeout;
        
        public Lock getLock() {
            com.fqm.framework.locks.Lock lock = lockFactory.getLockTemplate(lockMode).getLock(key);
            return new Lock(lock, block, acquireTimeout);
        }
        public String getKey() {
            return this.key;
        }
        public LockMode getLockMode() {
            return this.lockMode;
        }
    }
    
    public class Lock {
        com.fqm.framework.locks.Lock proxyLock;
        boolean block;
        long acquireTimeout;
        public Lock(com.fqm.framework.locks.Lock lock, boolean block, long acquireTimeout) {
            this.proxyLock = lock;
            this.block = block;
            this.acquireTimeout = acquireTimeout;
        }
        public boolean lock() {
            if (block) {
                proxyLock.lock();
                return true;
            } else if (acquireTimeout <= 0) {
                return proxyLock.tryLock();
            } else if (acquireTimeout > 0) {
                return proxyLock.tryLock(acquireTimeout, TimeUnit.MILLISECONDS);
            }
            return false;
        }
        public boolean unLock() {
            return proxyLock.unlock();
        }
    }
    
}
