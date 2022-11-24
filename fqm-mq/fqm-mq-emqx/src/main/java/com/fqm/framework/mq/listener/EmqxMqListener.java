/*
 * @(#)EmqxMqListener.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-mq-emqx
 * 创建日期 : 2022年11月21日
 * 修改历史 : 
 *     1. [2022年11月21日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emqx消息队列监听，基于 MQTT 协议
 * @version 
 * @author 傅泉明
 */
public class EmqxMqListener extends MqListenerAdapter<String> implements MqttCallbackExtended {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /** 消息主题 */
    private String topic;
    /** 消费者组 */
    private String group;
    /** 死信消息主题 */
    private String deadTopic;
    /** 监听消息的 client */
    private MqttClient client;
    
    public MqttClient getClient() {
        return client;
    }

    public void setClient(MqttClient client) {
        this.client = client;
    }

    public EmqxMqListener(Method method, Object bean, MqttClient client, 
            String topic, String group) {
        super(method, bean);
        this.topic = topic;
        this.client = client;
        this.group = group;
        this.deadTopic = topic + ".DLQ";
    }
    
    /** 连接失败 */
    @Override
    public void connectionLost(Throwable cause) {
        // do nothing
    }
    
    /** 收到消息 */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            // 业务处理
            super.receiveMessage(new String(message.getPayload(), StandardCharsets.UTF_8));
            // 确认消息，client 必须是和自己EmqxMqListerner绑定的
            client.messageArrivedComplete(message.getId(), message.getQos());
        } catch (Exception e) {
            e.printStackTrace();
            // 业务异常，进入死信队列
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getPayload());
            MqttTopic mqttTopic = client.getTopic(deadTopic);
            MqttDeliveryToken token;
            try {
                token = mqttTopic.publish(mqttMessage);
                token.waitForCompletion();
                // 确认消息，client 必须是和自己EmqxMqListerner绑定的
                client.messageArrivedComplete(message.getId(), message.getQos());
            } catch (MqttException e1) {
                e1.printStackTrace();
            }
            // throw e 会导致 连接中断！，
        }
    }
    
    /** 消息发送成功 */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // do nothing
    }
    
    /** 连接成功 */
    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        // 订阅主题
        try {
            String subscribeTopic = "$share/" + group + "/" + topic;
            logger.info("subscribe topic:{}", subscribeTopic);
            client.subscribe(subscribeTopic, 1);
        } catch (MqttException e) {
            logger.error("emqx connect error", e);
        }
    }

}
