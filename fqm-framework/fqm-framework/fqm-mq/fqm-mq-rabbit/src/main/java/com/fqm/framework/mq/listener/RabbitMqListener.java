package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import com.fqm.framework.mq.template.RabbitMqTemplate;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

/**
 * Rabbit消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitMqListener extends MqListenerAdapter<String> implements ChannelAwareMessageListener {

    /** 主题 */
    private String destination;
    private RabbitMqTemplate rabbitMqTemplate;

    public RabbitMqListener(Object bean, Method method, String destination,
            RabbitMqTemplate rabbitMqTemplate) {
        super(method, bean);
        this.destination = destination;
        this.rabbitMqTemplate = rabbitMqTemplate;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        byte[] body = message.getBody();
        String bodyStr = new String(body);
        // 消息头属性
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            super.receiveMessage(bodyStr);
            /** 手动发送ack确认收到消息 */
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // TODO 不是事务操作
            /** 进入死信队列 */
            String deadTopic = destination + ".DLQ";
            rabbitMqTemplate.initTopic(deadTopic, false);
            
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            // 消息持久化
            builder.contentType("text/plain").deliveryMode(2);
            channel.basicPublish("", deadTopic, builder.build(), body);
            
            /** 手动发送Reject不收消息，不放回队列 */
            channel.basicReject(deliveryTag, false);
            throw e;
        }

    }
}
