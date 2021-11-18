package com.fqm.framework.common.mq.listener;

import java.lang.reflect.Method;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * Kafka消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class KafkaMqListener extends MqListenerAdapter<String> implements AcknowledgingMessageListener<String, String> {

    public KafkaMqListener(Object bean, Method method) {
        setBean(bean);
        setMethod(method);
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        try {
            super.receiveMessage(data.value());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
