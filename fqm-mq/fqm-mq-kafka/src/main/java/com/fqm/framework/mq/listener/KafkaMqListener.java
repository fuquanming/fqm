package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import com.fqm.framework.mq.exception.MqException;

/**
 * Kafka消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class KafkaMqListener extends MqListenerAdapter<String> implements AcknowledgingMessageListener<String, String> {

    public KafkaMqListener(Object bean, Method method) {
        super(method, bean);
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        try {
            super.receiveMessage(data.value());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            throw new MqException(e);
        }
    }
}
