/*
 * @(#)LockProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-lock
 * 创建日期 : 2023年3月1日
 * 修改历史 : 
 *     1. [2023年3月1日]创建文件 by 傅泉明
 */
package com.fqm.framework.locks.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fqm.framework.locks.LockMode;

/**
 * 锁的配置信息
 * @version 
 * @author 傅泉明
 */
public class LockProperties {
    /** 是否开启，默认为 true 开启 */
    private Boolean enabled = true;
    /** 校验加载的锁组件，默认为 true */
    private Boolean verify = true;
    /** 锁方式，指定所有锁的方式 */
    private LockMode binder;
    /** 锁配置，key：业务名称(多个锁方式共存时，同一个锁有不同的调用方式) */
    private Map<String, LockConfigurationProperties> locks = new LinkedHashMap<>();
    
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getVerify() {
        return verify;
    }

    public void setVerify(Boolean verify) {
        this.verify = verify;
    }

    public LockMode getBinder() {
        return binder;
    }

    public void setBinder(LockMode binder) {
        this.binder = binder;
    }

    public Map<String, LockConfigurationProperties> getLocks() {
        return locks;
    }

    public void setLocks(Map<String, LockConfigurationProperties> locks) {
        this.locks = locks;
    }
    
}
