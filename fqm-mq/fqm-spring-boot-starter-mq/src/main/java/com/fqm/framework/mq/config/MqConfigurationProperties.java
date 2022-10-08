/*
 * @(#)MqConfigurationProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-mq
 * 创建日期 : 2022年9月7日
 * 修改历史 : 
 *     1. [2022年9月7日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.config;

/**
 * 消息自动注册配置
 * @version 
 * @author 傅泉明
 */
public class MqConfigurationProperties {
    /** 消息名称，该值等于自定义注解@MqListener.name()，唯一值 */
    private String name;
    /** 消息主题，必填 */
    private String topic;
    /** 消费者组，使用@MqListener时，必填 */
    private String group;
    /** 消息的组件名 参考@MqMode，必填 */
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
