package com.fqm.framework.common.redis.listener.spring;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyspaceEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.Nullable;

/**
 * Redis key新增、更新监听事件，将监听到的事件，在通过spring发布事件
 * 使用注解 {@link EventListener} 监听{@link RedisKeyUpdateEvent}消息
 * 
 * @version 
 * @author 傅泉明
 */
public class KeyUpdateEventMessageListener extends KeyspaceEventMessageListener implements ApplicationEventPublisherAware {

    /** 发布事件 */
    @Nullable
    private ApplicationEventPublisher publisher;

    public KeyUpdateEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    protected void doRegister(RedisMessageListenerContainer listenerContainer) {
        /** 监听的主题 */
        listenerContainer.addMessageListener(this, TopicManager.getUpdateTopic());
    }

    @Override
    protected void doHandleMessage(Message message) {
        // 发布事件
        this.publishEvent(new RedisKeyUpdateEvent(message.getBody()));
    }

    protected void publishEvent(RedisKeyUpdateEvent event) {
        if (this.publisher != null) {
            this.publisher.publishEvent(event);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
    
}
