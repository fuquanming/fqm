/*
 * @(#)EmqxMqProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : test-module-mq-emqx
 * 创建日期 : 2022年12月23日
 * 修改历史 : 
 *     1. [2022年12月23日]创建文件 by 傅泉明
 */
package com.fqm.test.mq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@ConfigurationProperties(prefix = "mq.mqs.g")
public class EmqxMqProperties {
    
    private String name;
    private String topic;
    private String group;
    private String binder;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public String getBinder() {
        return binder;
    }
    public void setBinder(String binder) {
        this.binder = binder;
    } 
}
