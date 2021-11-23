package com.fqm.framework.common.mq.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private StringRedisTemplate stringRedisTemplate;
    
    public RedisMqTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = null;
        try {
            str = getJsonStr(msg);
            logger.info("RedisMqProducer->{},{}", topic, str);
            /**
             * 查看数据：XRANGE topic - +
             * 如：payload为key
             * 1) 1) "1637140659049-0"
                  2) 1) "payload"
                     2) "{\"age\":1,\"name\":\"\xe5\xbc\xa0\xe4\xb8\x89\"}"
             * 添加数据：XADD topic * key value
             */
            RecordId recordId1 = stringRedisTemplate.opsForStream().add(
                    StreamRecords.newRecord().ofObject(str).withStreamKey(topic));
            if (recordId1.getSequence() != null) {
                logger.info("RedisMqProducer.success->topic=[{}],message=[{}],offset=[{}]", topic, str, recordId1.getSequence());
                return true;
            } else {
                logger.error("RedisMqProducer.error->topic=[{}],message=[{}],offset=[{}]", topic, str, recordId1);
            }
        } catch (Exception e) {
            logger.error("RedisMqProducer-error->" + topic + "," + str, e);
            e.printStackTrace();
        }
        return false;
    }

}
