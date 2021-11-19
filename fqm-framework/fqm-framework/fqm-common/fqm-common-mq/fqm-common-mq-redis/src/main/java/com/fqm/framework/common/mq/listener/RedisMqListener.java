package com.fqm.framework.common.mq.listener;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
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

    @SuppressWarnings("rawtypes")
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        // 消费消息
        try {
            super.receiveMessage(message.getValue().values().iterator().next());
            SessionCallback<List<?>> sessionCallback = new SessionCallback<List<?>>() {
                @Override
                public List execute(RedisOperations operations) throws DataAccessException {
                    /** 
                     * 开启事务，消费成功->删除消息并回复ACK
                     * 未ACK的数据会保存在消费者组里查看该组未ACK的数据：XPENDING topic group
                     */
                    operations.multi();
                    redisTemplate.opsForStream().delete(destination, message.getId());
                    redisTemplate.opsForStream().acknowledge(destination, group, message.getId());
                    return operations.exec();
                }
            };
            redisTemplate.execute(sessionCallback);
        } catch (Exception e) {
            SessionCallback<List<?>> sessionCallback = new SessionCallback<List<?>>() {
                @Override
                public List execute(RedisOperations operations) throws DataAccessException {
                    /** 开启事务，消费失败
                     * 1.删除消息并回复ACK
                     * 2.将消息存入死信队列，topic + ".DLQ"
                     */
                    operations.multi();
                    redisTemplate.opsForStream().delete(destination, message.getId());
                    redisTemplate.opsForStream().acknowledge(destination, group, message.getId());
                    redisTemplate.opsForStream().add(StreamRecords.newRecord()
                            .ofObject(message.getValue().values().iterator().next())
                            .withStreamKey(destination + ".DLQ")
                            );
                    return operations.exec();
                }
            };
            redisTemplate.execute(sessionCallback);
            throw new RuntimeException(e);
        }
        
    }

}
