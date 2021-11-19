package com.fqm.framework.common.mq.template;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Rabbit消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RabbitTemplate rabbitTemplate;
    private AmqpAdmin amqpAdmin;
    
    private Set<String> topicSet = new HashSet<>();
    
    public RabbitMqTemplate(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        try {
            initTopic(topic);
            rabbitTemplate.convertAndSend("", topic, str);
            logger.info("RabbitMqProducer.success->topic=[{}],message=[{}]", topic, str);
            return true;
        } catch (Exception e) {
            logger.error("RabbitMqProducer.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    public void initTopic(String topic) {
        if (!topicSet.contains(topic)) {
            // String name:名称, boolean durable:是否持久化, boolean exclusive:是否排他（只能一个人连接）, boolean autoDelete:是否自动删除
            String queueResult = amqpAdmin.declareQueue(new Queue(topic, true, false, false, null));
            logger.info("queueInit=" + queueResult);
            topicSet.add(topic);
        }
    }

}
