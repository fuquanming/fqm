/*
 * @(#)MqProducer.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-mq
 * 创建日期 : 2022年12月25日
 * 修改历史 : 
 *     1. [2022年12月25日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.fqm.framework.common.core.exception.ErrorCode;
import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.template.MqTemplate;

/**
 * 消息发送者，通过业务名称发送消息
 * @version 
 * @author 傅泉明
 */
public class MqProducer {
    
    /** 消息工厂 */
    private MqFactory mqFactory;
    /** 消息配置 */
    private MqProperties mqProperties;
    /** 业务消息生产者，key：业务名称 */
    private Map<String, Producer> producerMap;
    
    public MqProducer(MqFactory mqFactory, MqProperties mqProperties) {
        this.mqFactory = mqFactory;
        this.mqProperties = mqProperties;
        producerMap = new ConcurrentHashMap<>(mqProperties.getMqs().size());
        init();
    }
    /** 初始化 */
    private void init() {
        // 1、初始化业务对应的消息生产者
        for (Map.Entry<String, MqConfigurationProperties> entry : mqProperties.getMqs().entrySet()) {
            String businessName = entry.getKey();
            MqConfigurationProperties mcp = entry.getValue();
            Producer producer = new Producer();
            producer.topic = mcp.getTopic();
            producer.mqMode = mcp.getBinder();
            producerMap.put(businessName, producer);
        }
    }
    
    private MqConfigurationProperties getMqConfigurationProperties(String businessName) {
        MqConfigurationProperties mqConfigurationProperties = mqProperties.getMqs().get(businessName);
        if (null == mqConfigurationProperties) {
            throw new ServiceException(new ErrorCode(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未配置该业务的消息送通道"));
        }
        return mqConfigurationProperties;
    }
    
    /**
     * 获取消息模板
     * @param businessName
     * @return
     */
    public MqTemplate getMqTemplate(String businessName) {
        return mqFactory.getMqTemplate(getBinder(businessName));
    }
    
    /**
     * 获取消息组件
     * @param businessName 业务名称
     * @return
     */
    public MqMode getBinder(String businessName) {
        return getMqConfigurationProperties(businessName).getBinder();
    }
    
    /**
     * 获取消息主题
     * @param businessName 业务名称
     * @return
     */
    public String getTopic(String businessName) {
        return getMqConfigurationProperties(businessName).getTopic();
    }
    
    /**
     * 获取消息生产者
     * @param businessName  业务名称
     * @return
     */
    public Producer getProducer(String businessName) {
        Producer producer = producerMap.get(businessName);
        if (null == producer) {
            throw new ServiceException(new ErrorCode(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未找到该业务的消息通道，" + businessName));
        }
        return producer;
    }
    /**
     * 消息发送 
     * @version 
     * @author 傅泉明
     */
    public class Producer {
        MqMode mqMode;
        String topic;
        /**
         * 同步发送消息，对象使用json保存到队列中
         * @param msg   消息
         * @return
         */
        public boolean syncSend(Object msg) {
            return mqFactory.getMqTemplate(mqMode).syncSend(topic, msg);
        }
        
        /**
         * 同步发送延迟消息，对象使用json保存到队列中
         * @param msg           消息
         * @param delayTime     延迟时间
         * @param timeUnit      延时时间单位
         * @return
         */
        public boolean syncDelaySend(Object msg, int delayTime, TimeUnit timeUnit) {
            return mqFactory.getMqTemplate(mqMode).syncDelaySend(topic, msg, delayTime, timeUnit);
        }
        /**
         * 异步发送消息，对象使用json保存到队列中
         * @param msg
         * @param sendCallback
         */
        public void asyncSend(Object msg, SendCallback sendCallback) {
            mqFactory.getMqTemplate(mqMode).asyncSend(topic, msg, sendCallback);
        }
    }
}
