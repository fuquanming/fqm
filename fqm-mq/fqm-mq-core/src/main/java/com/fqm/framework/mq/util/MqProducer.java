/*
 * @(#)MqProducer.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-mq
 * 创建日期 : 2022年12月25日
 * 修改历史 : 
 *     1. [2022年12月25日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.util;

import com.fqm.framework.common.core.exception.ErrorCode;
import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.config.MqConfigurationProperties;
import com.fqm.framework.mq.config.MqProperties;
import com.fqm.framework.mq.template.MqTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 消息发送者
 * @version 
 * @author 傅泉明
 */
public class MqProducer {

    private MqFactory mqFactory;
    private MqProperties mqProperties;
    
    public MqProducer(MqFactory mqFactory, MqProperties mqProperties) {
        this.mqFactory = mqFactory;
        this.mqProperties = mqProperties;
    }
    
    private MqConfigurationProperties getMqConfigurationProperties(String businessName) {
        MqConfigurationProperties mqConfigurationProperties = mqProperties.getMqs().get(businessName);;
        if (null == mqConfigurationProperties) {
            throw new ServiceException(new ErrorCode(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未配置该业务的发送通道"));
        }
        return mqConfigurationProperties;
    }
    /**
     * 获取消息模板
     * @param businessName
     * @return
     */
    public MqTemplate getMqTemplate(String businessName) {
        MqConfigurationProperties mqConfigurationProperties = getMqConfigurationProperties(businessName);
        // 消息组件
        String binder = mqConfigurationProperties.getBinder();
        return mqFactory.getMqTemplate(binder);
    }
    /**
     * 获取消息组件
     * @param businessName
     * @return
     */
    public String getBinder(String businessName) {
        return getMqConfigurationProperties(businessName).getBinder();
    }
    /**
     * 获取消息主题 
     * @param businessName
     * @return
     */
    public String getTopic(String businessName) {
        return getMqConfigurationProperties(businessName).getTopic();
    }
    
    /**
     * 同步发送消息，对象使用json保存到队列中
     * @param businessName  业务名称
     * @param msg   消息
     * @return
     */
    public boolean syncSend(String businessName, Object msg) {
        return getMqTemplate(businessName).syncSend(getTopic(businessName), msg);
    }
    
    /**
     * 同步发送延迟消息，对象使用json保存到队列中
     * @param businessName  业务名称s
     * @param msg           消息
     * @param delayTime     延迟时间
     * @param timeUnit      延时时间单位
     * @return
     */
    public boolean syncDelaySend(String businessName, Object msg, int delayTime, TimeUnit timeUnit) {
        return getMqTemplate(businessName).syncDelaySend(getTopic(businessName), msg, delayTime, timeUnit);
    }
    /**
     * 异步发送消息，对象使用json保存到队列中
     * @param businessName  业务名称 
     * @param msg
     * @param sendCallback
     */
    public void asyncSend(String businessName, Object msg, SendCallback sendCallback) {
        getMqTemplate(businessName).asyncSend(getTopic(businessName), msg, sendCallback);
    }
    
}
