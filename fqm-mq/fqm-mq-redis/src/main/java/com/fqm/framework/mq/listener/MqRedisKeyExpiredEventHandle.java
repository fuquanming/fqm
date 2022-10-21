package com.fqm.framework.mq.listener;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.fqm.framework.common.redis.listener.spring.RedisKeyExpiredEvent;
import com.fqm.framework.mq.constant.Constants;

/**
 * 监听Mq中过期的消息，从hashmap中获取消息投递到消费者中
 * 
 * @version 
 * @author 傅泉明
 */
public class MqRedisKeyExpiredEventHandle {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final RedisScript<String> SCRIPT_DELAY_MESSAGE_DELIVERY = 
            new DefaultRedisScript<>(
                    // 获取hashmap消息
                    "local msg = redis.call('hget', KEYS[1], ARGV[1]) " +   
                    "if msg==false or msg[1]==false then" + 
                    "    return '0' " + 
                    "else " + 
                    "    redis.call('xadd', KEYS[2], 'MAXLEN', '~', ARGV[2], '*', 'dm', msg) " +    // 投递消息到队列
                    "    redis.call('hdel', KEYS[1], ARGV[1]) " +           // 删除hashmap消息
                    "    return '1' " +
                    "end"
                    , String.class
                    );
    
    private StringRedisTemplate stringRedisTemplate;

    public MqRedisKeyExpiredEventHandle(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @EventListener
    public void expiredEventHandle(RedisKeyExpiredEvent event) {
        String expiredKey = new String(event.getSource());
//        logger.info("expiredEventHandle=" + expiredKey);
        if (expiredKey.startsWith(Constants.DELAY_MESSAGE_TTL_PREFIX_KEY)) {
            String[] topicInfos = expiredKey.split(Constants.DELAY_MESSAGE_TTL_PREFIX_KEY);
            String topicInfo = topicInfos[1];
            int separatorIndex = topicInfo.lastIndexOf("-");
            String topic = topicInfo.substring(0, separatorIndex);
            String id = topicInfo.substring(separatorIndex + 1);

            // 只能投递一次，多个消费者都会收到通知
//            String ttlTopicId = Constants.DELAY_MESSAGE_INCR_PREFIX_KEY + topic;
//            long ttlIncr = stringRedisTemplate.opsForHash().increment(ttlTopicId, id, 1);
//            if (ttlIncr == 1) {
//                // 获取的消息，可能和通知的id不一样，通知会有延迟或延迟相同时间的消息
//                String topicHashMap = Constants.DELAY_MESSAGE_HASHMAP_PREFIX_KEY + topic;
//                Object msgObj = stringRedisTemplate.opsForHash().get(topicHashMap, id);
//                if (msgObj != null) {
//                    logger.info("expired delayMessageKey=" + expiredKey);
//                    String msg = msgObj.toString();
//                    // 投递消息
//                    RecordId recordId1 = stringRedisTemplate.opsForStream().add(StreamRecords.newRecord().ofObject(msg).withStreamKey(topic));
//                    if (recordId1.getSequence() != null) {
//                        // hashmap删除消息
//                        stringRedisTemplate.opsForHash().delete(topicHashMap, id);
//                    }
//                }
//            }
//            // 删除自增id
//            stringRedisTemplate.opsForHash().delete(ttlTopicId, id);
            
            // Lua
            Object delayMessageFlag = stringRedisTemplate.execute(
                    SCRIPT_DELAY_MESSAGE_DELIVERY,
                    stringRedisTemplate.getStringSerializer(),
                    stringRedisTemplate.getStringSerializer(),
                    Arrays.asList(Constants.DELAY_MESSAGE_HASHMAP_PREFIX_KEY + topic, topic),
                    id, String.valueOf(Constants.MAX_QUEUE_SIZE)
                    );
            boolean flag = Objects.equals(delayMessageFlag.toString(), "1");
            if (flag) {
                logger.info("expired delayMessageKey=" + expiredKey);
            }
        }
    }

}