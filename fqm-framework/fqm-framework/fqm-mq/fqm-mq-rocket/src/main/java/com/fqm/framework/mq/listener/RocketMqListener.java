package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;

import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * Rocket消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class RocketMqListener extends MqListenerAdapter<String> implements RocketMQListener<String> {

    public RocketMqListener(Object bean, Method method) {
        super(method, bean);
    }
    
    @Override
    public void onMessage(String message) {
        try {
            super.receiveMessage(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
