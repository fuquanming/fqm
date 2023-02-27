package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import com.rabbitmq.client.Channel;

/**
 * Rabbit消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitMqListener extends MqListenerAdapter<String> implements ChannelAwareMessageListener {

    public RabbitMqListener(Object bean, Method method) {
        super(method, bean);
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        byte[] body = message.getBody();
        String bodyStr = new String(body, StandardCharsets.UTF_8);
        // 消息头属性
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            super.receiveMessage(bodyStr);
            /** 手动发送ack确认收到消息 */
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            /** 手动发送Reject不收消息，不放回队列 */
            channel.basicReject(deliveryTag, false);
            throw e;
        }

    }
}
