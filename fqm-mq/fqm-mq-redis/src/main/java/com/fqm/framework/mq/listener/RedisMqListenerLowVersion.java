package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

import com.fqm.framework.mq.exception.MqException;
/**
 * Redis消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisMqListenerLowVersion extends MqListenerAdapter<String> implements StreamListener<String, MapRecord<String, String, String>> {

    private StringRedisTemplate redisTemplate;
    /** 主题 */
    private String destination;
    /** 消费组 */
    private String group;
    
    public RedisMqListenerLowVersion(Object bean, Method method, 
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
            SessionCallback<List<?>> sessionCallback = new SessionCallback<List<?>>() {
                @Override
                public List execute(RedisOperations operations) throws DataAccessException {
                    /** 
                     * 开启事务，消费成功->回复ACK，不删除消息（不同组，都会收到消息，消息会一直存在XRANGE my-topic - + 中）
                     * 未ACK的数据会保存在消费者组里查看该组未ACK的数据：XPENDING topic group
                     */
                    operations.multi();
                    operations.opsForStream().acknowledge(destination, group, recordId);
                    operations.opsForStream().delete(destination, recordId);
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
                    operations.opsForStream().acknowledge(destination, group, recordId);
                    operations.opsForStream().add(StreamRecords.newRecord()
                            .ofObject(msg)
                            .withStreamKey(destination + ".DLQ")
                            );
                    operations.opsForStream().delete(destination, recordId);
                    return operations.exec();
                }
            };
            redisTemplate.execute(sessionCallback);
            throw new MqException(e);
        }
        
    }

}
