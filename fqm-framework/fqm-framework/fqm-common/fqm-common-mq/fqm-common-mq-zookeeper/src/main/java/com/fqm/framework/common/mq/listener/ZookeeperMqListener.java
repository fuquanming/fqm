package com.fqm.framework.common.mq.listener;

import java.lang.reflect.Method;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;

/**
 * Zookeeper消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class ZookeeperMqListener extends MqListenerAdapter<String> implements QueueConsumer<String> {

    private DistributedQueue<String> deadQueue;
    
    public ZookeeperMqListener(Object bean, Method method, DistributedQueue<String> deadQueue) {
        super(method, bean);
        this.deadQueue = deadQueue;
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {

    }

    @Override
    public void consumeMessage(String message) throws Exception {
        try {
            super.receiveMessage(message);
        } catch (Exception e) {
            // 入死信队列
            if (deadQueue != null) deadQueue.put(message);
            throw new RuntimeException(e);
        }
    }
}
