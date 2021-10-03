package com.fqm.framework.common.redis.listener.spring;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.Nullable;

/**
 * Redis 监听事件
 * 参考 KeyExpirationEventMessageListener 过期事件
 * 继承该类，重写 onMessage 方法
 * @version 
 * @author 傅泉明
 */
public abstract class KeyEventMessageListener extends KeyspaceEventMessageListener implements ApplicationEventPublisherAware {
    
    /** 监听的主题 */
    private Topic topic;

    @Nullable
    private ApplicationEventPublisher publisher;

    public KeyEventMessageListener(RedisMessageListenerContainer listenerContainer, Topic topic) {
        super(listenerContainer);
        this.topic = topic;
    }

    protected void doRegister(RedisMessageListenerContainer listenerContainer) {
        listenerContainer.addMessageListener(this, topic);
    }

    protected void doHandleMessage(Message message) {
        this.publishEvent(new RedisKeyExpiredEvent<Object>(message.getBody()));
    }

    protected void publishEvent(RedisKeyExpiredEvent<?> event) {
        if (this.publisher != null) {
            this.publisher.publishEvent(event);
        }
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        handleMessage(message, pattern);
    }
    /**
     * 收到事件的消息
     * @param message
     * @param pattern
     */
    public abstract void handleMessage(Message message, byte[] pattern);
    
}