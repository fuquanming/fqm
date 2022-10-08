package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;

import com.fqm.framework.mq.template.ZookeeperMqTemplate;

/**
 * Zookeeper消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class ZookeeperMqListener extends MqListenerAdapter<String> implements QueueConsumer<String> {

    private ZookeeperMqTemplate zookeeperMqTemplate;
    
    private String topic;
    
    public ZookeeperMqListener(Object bean, Method method, ZookeeperMqTemplate zookeeperMqTemplate, 
            String topic) {
        super(method, bean);
        this.zookeeperMqTemplate = zookeeperMqTemplate;
        this.topic = topic;
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
            zookeeperMqTemplate.syncSend(topic + ".DLQ", message);
            throw new RuntimeException(e);
        }
    }
}
