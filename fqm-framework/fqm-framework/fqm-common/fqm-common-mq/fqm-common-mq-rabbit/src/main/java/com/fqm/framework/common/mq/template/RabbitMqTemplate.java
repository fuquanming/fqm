package com.fqm.framework.common.mq.template;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fqm.framework.common.mq.callback.RabbitListenableFutureCallback;
import com.fqm.framework.common.mq.client.producer.SendCallback;

import cn.hutool.system.SystemUtil;

/**
 * Rabbit消息队列，发送都是异步消息
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RabbitTemplate rabbitTemplate;
    private AmqpAdmin amqpAdmin;
    
    private Set<String> topicSet = new HashSet<>();
    
    private Map<String, SendCallback> sendCallbackMap = new ConcurrentHashMap<>();
    private Map<String, RabbitListenableFutureCallback> futureCallbackMap = new ConcurrentHashMap<>();

    private AtomicLong atomicLong = new AtomicLong(); 
    
    public RabbitMqTemplate(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }
    
    public Map<String, SendCallback> getSendCallbackMap() {
        return sendCallbackMap;
    }
    
    public Map<String, RabbitListenableFutureCallback> getFutureCallbackMap() {
        return futureCallbackMap;
    }
    
    public void initTopic(String topic) {
        if (!topicSet.contains(topic)) {
            // String name:名称, boolean durable:是否持久化, boolean exclusive:是否排他（只能一个人连接）, boolean autoDelete:是否自动删除
            String queueResult = amqpAdmin.declareQueue(new Queue(topic, true, false, false, null));
            logger.info("queueInit=" + queueResult);
            topicSet.add(topic);
        }
    }
    /**
     * 消息ID
     * @return
     */
    private String getId() {
        return String.format("%s@%d@%s", SystemUtil.getHostInfo().getAddress(), 
                SystemUtil.getCurrentPID(), atomicLong.incrementAndGet());
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        String id = getId();
        try {
//            initTopic(topic);
            CorrelationData correlationData = new CorrelationData(id);
            RabbitListenableFutureCallback callback = new RabbitListenableFutureCallback(Thread.currentThread(), id);
            correlationData.getFuture().addCallback(callback);
            
            futureCallbackMap.put(id, callback);
            
            rabbitTemplate.convertAndSend("", topic
//                    + "111"
                    , str, correlationData);
            
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));// 最多等3秒
            if (callback.isMsgError()) {
                logger.info(Thread.currentThread().getId() + ",RabbitMqProducer.error->topic=[{}],message=[{}]", topic, str);
                return false;
            } else {
                logger.info(Thread.currentThread().getId() + ",RabbitMqProducer.success->topic=[{}],message=[{}]", topic, str);
                return true;
            }
        } catch (Exception e) {
            logger.error(Thread.currentThread().getId() + ",RabbitMqProducer.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        } finally {
            futureCallbackMap.remove(id);
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        String id = getId();
        try {
//            initTopic(topic);
//            sendCallbackMap.put(id, sendCallback);
            
            CorrelationData correlationData = new CorrelationData(id);
            RabbitListenableFutureCallback callback = new RabbitListenableFutureCallback(Thread.currentThread(), id, sendCallback);
            correlationData.getFuture().addCallback(callback);
            
            futureCallbackMap.put(id, callback);
            
            rabbitTemplate.convertAndSend("", topic
//                    + "111"
                    , str, correlationData);
            logger.info(Thread.currentThread().getId() + ",RabbitMqProducer->topic=[{}],message=[{}]", topic, str);
        } catch (Exception e) {
            logger.error(Thread.currentThread().getId() + ",RabbitMqProducer.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
    }
    
}