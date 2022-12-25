/*
 * @(#)EmqxMqAutoConfiguration.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-mq-emqx
 * 创建日期 : 2022年11月23日
 * 修改历史 : 
 *     1. [2022年11月23日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.config;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.client.EmqxClient;
import com.fqm.framework.mq.exception.MqException;
import com.fqm.framework.mq.listener.EmqxMqListener;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.template.EmqxMqTemplate;

/**
 * Emqx消息队列自动装配
 * MqProperties加载，并在MqAutoConfiguration后加载
 * SmartInitializingSingleton接口在Bean加载完成后，加载EmqxMq
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class EmqxMqAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;
    
    @Value("${server.port:}")
    private String port;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    } 

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "emqx")
    EmqxProperties emqxProperties() {
        return new EmqxProperties();
    }

    /**
     * 发送消息的客户端
     * @param properties
     * @return
     */
    @Bean(name = "emqxClient", initMethod = "connect", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    EmqxClient emqxClient(EmqxProperties properties) {
        EmqxClient emqxClient = null;
        try {
            emqxClient = new EmqxClient(properties);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return emqxClient;
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(300)
    EmqxMqTemplate emqxMqTemplate(MqFactory mqFactory, EmqxClient emqxClient) {
        EmqxMqTemplate emqxMqTemplate = new EmqxMqTemplate(emqxClient.getMqttClient());
        mqFactory.addMqTemplate(emqxMqTemplate);
        return emqxMqTemplate;
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        MqListenerAnnotationBeanPostProcessor mq = applicationContext.getBean(MqListenerAnnotationBeanPostProcessor.class);
        MqProperties mp = applicationContext.getBean(MqProperties.class);
        EmqxProperties emqxProperties = applicationContext.getBean(EmqxProperties.class);
        // 注册 监听消息的客户端
        int i = 0;
        for (MqListenerParam v : mq.getListeners()) {
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties != null && MqMode.EMQX.equalMode(properties.getBinder())) {
                String group = properties.getGroup();
                String topic = properties.getTopic();
                Assert.isTrue(StringUtils.isNotBlank(topic), "Please specific [topic] under mq.mqs." + name + " configuration.");
                Assert.isTrue(StringUtils.isNotBlank(group), "Please specific [group] under mq.mqs." + name + " configuration.");
                String beanName = "emqxListener." + i;
                // 动态注册
                //将applicationContext转换为ConfigurableApplicationContext
                ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
                // 获取bean工厂并转换为DefaultListableBeanFactory
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
                if (!applicationContext.containsBean(beanName)) {
                    
                    EmqxMqListener emqxMqListener = new EmqxMqListener(v.getMethod(), v.getBean(), null, topic, group);
                    
                    // 通过BeanDefinitionBuilder创建bean定义
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(EmqxClient.class);
                    beanDefinitionBuilder.addConstructorArgValue(emqxProperties);
                    beanDefinitionBuilder.addConstructorArgValue(emqxMqListener);
                    beanDefinitionBuilder.addConstructorArgValue(topic);
                    // 应用程序的端口号
                    beanDefinitionBuilder.addConstructorArgValue(port);
                    beanDefinitionBuilder.setDestroyMethodName("destroy");
                    // 注册bean
                    defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
                    // 实例化
                    EmqxClient client = (EmqxClient) applicationContext.getBean(beanName);
                    // 在 client 连接之前，初始化 EmqxMqListener 的 client
                    emqxMqListener.setClient(client.getMqttClient());
                    // 建立连接
                    try {
                        client.connect();
                    } catch (MqttException e) {
                        throw new MqException(e);
                    }
                    i++;
                    logger.info("Init EmqxMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
                }
            }
        }
    }

}
