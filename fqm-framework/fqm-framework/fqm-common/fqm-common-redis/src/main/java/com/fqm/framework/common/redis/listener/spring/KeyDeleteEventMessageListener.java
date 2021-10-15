package com.fqm.framework.common.redis.listener.spring;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;

/**
 * Redis key删除监听事件，将监听到的事件，在通过spring发布事件
 * 使用注解 {@link EventListener} 监听{@link RedisKeyDeleteEvent}消息
 * 
 * @version 
 * @author 傅泉明
 */
public class KeyDeleteEventMessageListener extends KeyspaceEventMessageListener implements ApplicationEventPublisherAware {

    /** 发布事件 */
    @Nullable
    private ApplicationEventPublisher publisher;

    public KeyDeleteEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    protected void doRegister(RedisMessageListenerContainer listenerContainer) {
        /** 监听的主题 */
        listenerContainer.addMessageListener(this, TopicManager.getDeleteTopic());
    }

    @Override
    protected void doHandleMessage(Message message) {
        // 发布事件
        this.publishEvent(new RedisKeyDeleteEvent(message.getBody()));
    }

    protected void publishEvent(RedisKeyDeleteEvent event) {
        if (this.publisher != null) {
            this.publisher.publishEvent(event);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
    
}
