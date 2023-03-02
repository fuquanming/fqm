/*
 * @(#)LockConfigurationProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-lock
 * 创建日期 : 2023年3月1日
 * 修改历史 : 
 *     1. [2023年3月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.locks.config;

import com.fqm.framework.locks.LockMode;

/**
 * 锁自动注册配置
 * @version 
 * @author 傅泉明
 */
public class LockConfigurationProperties {
    /** 锁名称，必填 */
    private String key;
    /** 锁的组件名，参考 @LockMode（无则取LockProperties.binder） */
    private LockMode binder;
    /** 是否阻塞获取锁 */
    private boolean block;
    /** 获取锁超时时间 单位：毫秒 */
    private long acquireTimeout;
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public LockMode getBinder() {
        return binder;
    }
    public void setBinder(LockMode binder) {
        this.binder = binder;
    }
    public boolean isBlock() {
        return block;
    }
    public void setBlock(boolean block) {
        this.block = block;
    }
    public long getAcquireTimeout() {
        return acquireTimeout;
    }
    public void setAcquireTimeout(long acquireTimeout) {
        this.acquireTimeout = acquireTimeout;
    }
}
