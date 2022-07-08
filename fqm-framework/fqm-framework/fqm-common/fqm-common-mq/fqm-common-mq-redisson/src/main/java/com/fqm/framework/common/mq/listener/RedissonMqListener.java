package com.fqm.framework.common.mq.listener;

import java.lang.reflect.Method;

import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;

import com.fqm.framework.common.core.util.StringUtil;
/**
 * Redisson消息队列监听器
 * 
 * @version 
 * @author 傅泉明
 */
public class RedissonMqListener extends MqListenerAdapter<String> implements Runnable {

    private RedissonClient redissonClient;
    /** 主题 */
    private String destination;
    /** 消费组 */
    private String group;
    
    private volatile boolean listenerFlag = true;
    
    public RedissonMqListener(Object bean, Method method, RedissonClient redissonClient, String destination, String group) {
        super(method, bean);
        this.redissonClient = redissonClient;
        this.destination = destination;
        this.group = group;
    }
    
    public void stop() {
        listenerFlag = false;
    }
    
    @Override
    public void run() {
        while (listenerFlag) {
            try {
                RBlockingDeque<String> topicQueue = redissonClient.getBlockingDeque(destination);
                redissonClient.getDelayedQueue(topicQueue);
                // 阻塞获取队列数据，没有ack消息队列，消息取出服务端就删除了该消息
                String msg = topicQueue.take().toString();
                if (StringUtil.isNotBlank(msg)) {
                    receiveMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
