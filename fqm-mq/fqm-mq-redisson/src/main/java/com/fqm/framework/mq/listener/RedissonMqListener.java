package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;

/**
 * Redisson消息队列监听器
 * 
 * @version 
 * @author 傅泉明
 */
public class RedissonMqListener extends MqListenerAdapter<String> implements Runnable {

    private RedissonClient redissonClient;

    /** 主题 */
    private String topic;

    private volatile boolean listenerFlag = true;

    public RedissonMqListener(Object bean, Method method, RedissonClient redissonClient, String topic) {
        super(method, bean);
        this.redissonClient = redissonClient;
        this.topic = topic;
    }

    public void stop() {
        listenerFlag = false;
    }

    @Override
    public void run() {
        while (listenerFlag) {
            String msg = null;
            try {
                RBlockingDeque<String> topicQueue = redissonClient.getBlockingDeque(topic);
                redissonClient.getDelayedQueue(topicQueue);
                msg = topicQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 阻塞获取队列数据，没有ack消息队列，消息取出服务端就删除了该消息
            if (StringUtils.isNotBlank(msg)) {
                try {
                    receiveMessage(msg);
                } catch (Exception e) {
                    /** 进入死信队列 */
                    RBlockingDeque<Object> topicDlqQueue = redissonClient.getBlockingDeque(topic + ".DLQ");
                    RDelayedQueue<Object> delayedDlqQueue = redissonClient.getDelayedQueue(topicDlqQueue);
                    delayedDlqQueue.offer(msg, 0, TimeUnit.SECONDS);
                    e.printStackTrace();
                }
            }
        }
    }
}
