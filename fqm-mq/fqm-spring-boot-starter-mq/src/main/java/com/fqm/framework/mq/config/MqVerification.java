/*
 * @(#)MqVerification.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-mq
 * 创建日期 : 2023年3月14日
 * 修改历史 : 
 *     1. [2023年3月14日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.Assert;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;

/**
 * mq校验
 * 1、校验配置文件
 * 2、判断配置文件中是否加载锁组件
 * mq.binder 或者 mq.mqs.xxx.binder
 * @version 
 * @author 傅泉明
 */
public class MqVerification implements SmartInitializingSingleton {
    
    private MqFactory mqFactory;

    private MqProperties mqProperties;

    public MqVerification(MqFactory mqFactory, MqProperties mqProperties) {
        this.mqFactory = mqFactory;
        this.mqProperties = mqProperties;
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        // 1、校验配置文件
        MqMode lockMode = mqProperties.getBinder();
        mqProperties.getMqs().entrySet().forEach(entry -> {
            String businessName = entry.getKey();
            MqConfigurationProperties mcp = entry.getValue();

            MqMode binder = mcp.getBinder();
            if (null == binder) {
                binder = lockMode;
            }
            Assert.notNull(binder, "Please specific [binder] under [mq.mqs." + businessName + "] configuration or [binder] under [mq] configuration.");
            // 校验 MqMode 
            Assert.isTrue(mqFactory.containsMqTemplate(binder), "Please specific [binder] under [mq.mqs." + businessName + "] configuration, not found [" + binder + "].");
        });
    }
}
