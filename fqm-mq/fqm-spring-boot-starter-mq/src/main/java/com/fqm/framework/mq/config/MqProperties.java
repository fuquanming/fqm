/*
 * @(#)MqProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-mq
 * 创建日期 : 2022年9月7日
 * 修改历史 : 
 *     1. [2022年9月7日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fqm.framework.mq.MqMode;

/**
 * Mq properties
 * @version 
 * @author 傅泉明
 */
public class MqProperties {
    
    /** 是否开启，默认为 true 开启 */
    private Boolean enabled = true;
    /** 消息队列方式，指定所有消息队列的方式 */
    private MqMode binder;
    /** 消息队列配置，key：业务名称(多个消息队列共存时或使用死信队列时) */
    private Map<String, MqConfigurationProperties> mqs = new LinkedHashMap<>();
    
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public MqMode getBinder() {
        return binder;
    }

    public void setBinder(MqMode binder) {
        this.binder = binder;
    }

    public Map<String, MqConfigurationProperties> getMqs() {
        return mqs;
    }

    public void setMqs(Map<String, MqConfigurationProperties> mqs) {
        this.mqs = mqs;
    }
    
}
