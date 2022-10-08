package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
/**
 * Redis消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisMqListener extends MqListenerAdapter<String> implements StreamListener<String, MapRecord<String, String, String>> {

    private StringRedisTemplate redisTemplate;
    /** 主题 */
    private String destination;
    /** 消费组 */
    private String group;
    
    public RedisMqListener(Object bean, Method method, 
            StringRedisTemplate redisTemplate, String destination, String group) {
        super(method, bean);
        this.redisTemplate = redisTemplate;
        this.destination = destination;
        this.group = group;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        // 消费消息
        String msg = message.getValue().values().iterator().next();
        RecordId recordId = message.getId();
        try {
            super.receiveMessage(msg);
            // 使用RedisMqDeadMessageTasker触发死信队列任务
            redisTemplate.opsForStream().acknowledge(destination, group, recordId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

}
