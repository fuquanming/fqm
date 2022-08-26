package com.fqm.framework.mq.template;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.mq.client.producer.SendCallback;
/**
 * Redisson消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public class RedissonMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RedissonClient redissonClient;
    
    public RedissonMqTemplate(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        try {
            RBlockingDeque<Object> topicQueue = redissonClient.getBlockingDeque(topic);
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(topicQueue);
            delayedQueue.offer(str, 0, TimeUnit.SECONDS);
            logger.info("RedissonMqProducer.syncSend.success->topic=[{}],message=[{}]", topic, str);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("RedissonMqProducer.syncSend.error->topic=[{}],message=[{}]", topic, str);
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        try {
            RBlockingDeque<Object> topicQueue = redissonClient.getBlockingDeque(topic);
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(topicQueue);
            delayedQueue.offer(str, 0, TimeUnit.SECONDS);
            sendCallback.onSuccess(null);
            logger.info("RedissonMqProducer.asyncSend.success->topic=[{}],message=[{}]", topic, str);
        } catch (Exception e) {
            e.printStackTrace();
            sendCallback.onException(e);
            logger.error("RedissonMqProducer.asyncSend.error->topic=[{}],message=[{}]", topic, str);
        }
    }
    
    @Override
    public boolean syncDelaySend(String topic, Object msg, int delayTime, TimeUnit timeUnit) {
        String str = getJsonStr(msg);
        try {
            RBlockingDeque<Object> topicQueue = redissonClient.getBlockingDeque(topic);
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(topicQueue);
            delayedQueue.offer(str, delayTime, timeUnit);
            logger.info("RedissonMqProducer.syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
            return true;
        } catch (Exception e) {
            logger.error("RedissonMqProducer.syncDelaySend.error->topic=[" + topic + "],message=[" + str + "],delayTime=[" + delayTime + "],timeUnit=[" + timeUnit + "]", e);
            e.printStackTrace();
        }
        return false;
    }

}
