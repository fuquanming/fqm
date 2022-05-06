package com.fqm.framework.common.mq.template;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.fqm.framework.common.core.util.DateFormatUtil;
import com.fqm.framework.common.core.util.IdUtil;
import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.common.mq.constant.Constants;

/**
 * Redis消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private StringRedisTemplate stringRedisTemplate;
    /** 发送消息 */
    private static final RedisScript<String> SCRIPT_MESSAGE = 
            new DefaultRedisScript<>(
                    "return redis.call('xadd', KEYS[1], 'MAXLEN', '~', ARGV[1], '*', 'mq', ARGV[2]) "   // 投递消息到队列
                    , String.class
                    );
    
    // 延迟队列 EX：秒，PX：毫秒
    private static final RedisScript<String> SCRIPT_DELAY_MESSAGE = 
            new DefaultRedisScript<>(
                    "if redis.call('hset', KEYS[2], ARGV[3], ARGV[4]) == 1 then " + 
                    "    return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2]) " +
                    "else " + 
                    "    return 'fail' " + 
                    "end", String.class
                    );
    /** 获得延迟队列成功的值 */
    private static final String DELAY_MESSAGE_SUCCESS = "OK";
    
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
//            RecordId recordId1 = stringRedisTemplate.opsForStream().add(
//                    StreamRecords.newRecord().ofObject(str).withStreamKey(topic));
//            if (recordId1.getSequence() != null) {
//                logger.info("RedisMqProducer.success->topic=[{}],message=[{}],offset=[{}]", topic, str, recordId1.getSequence());
//                return true;
//            } else {
//                logger.error("RedisMqProducer.error->topic=[{}],message=[{}],offset=[{}]", topic, str, recordId1);
//            }
            // Lua
            Object messageFlag = stringRedisTemplate.execute(
                    SCRIPT_MESSAGE,
                    stringRedisTemplate.getStringSerializer(),
                    stringRedisTemplate.getStringSerializer(),
                    Collections.singletonList(topic),
                    String.valueOf(Constants.MAX_QUEUE_SIZE), str
                    );
            if (messageFlag != null) {
                logger.info("RedisMqProducer.success->topic=[{}],message=[{}],offset=[{}]", topic, str, messageFlag);
                return true;
            } else {
                logger.error("RedisMqProducer.error->topic=[{}],message=[{}],offset=[{}]", topic, str, messageFlag);
            }
        } catch (Exception e) {
            logger.error("RedisMqProducer-error->" + topic + "," + str, e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean syncDelaySend(String topic, Object msg, int delayTime, TimeUnit timeUnit) {
        String str = null;
        try {
            str = getJsonStr(msg);
            int time = (int) timeUnit.toMillis(delayTime);
            String timeStr = String.valueOf(time);
            
            String id = IdUtil.getSnowflake().nextIdStr();
            // 添加自定义属性
            Map<String, Object> msgMap = JsonUtil.toMap(str);
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MILLISECOND, time);
            HashMap<String, Object> info = new HashMap<>(2);
            /** 到期时间 */
            String expireTime = DateFormatUtil.format(c, "yyyy-MM-dd HH:mm:ss");
            // Zset里score的取值
//            String zsetScore = String.valueOf(c.getTimeInMillis());
            info.put(Constants.DELAY_MESSAGE_FIELD_TIME, expireTime);
            info.put(Constants.DELAY_MESSAGE_FIELD_ID, id);
            msgMap.put(Constants.DELAY_MESSAGE_FIELD_INFO, info);
            str = getJsonStr(msgMap);
            
            String msgStr = str;
            
            boolean flag = false;
            
            // 监听过期key，从zset中获取一个消息，多个监听者需要控制一个消息只能投递一次！，消费者必须做幂等性校验
//            SessionCallback<List<Boolean>> sessionCallback = new SessionCallback<List<Boolean>>() {
//                @Override
//                public List<Boolean> execute(RedisOperations operations) throws DataAccessException {
//                    /** 
//                     * 开启事务，设置消息过期时间
//                     * 消息保存到zset中
//                     */
//                    operations.multi();
//                    stringRedisTemplate.opsForValue().set(Constants.DELAY_MESSAGE_TTL_PREFIX_KEY + topic + "-" + id, expireTime, delayTime, timeUnit);
//                    stringRedisTemplate.opsForHash().put(Constants.DELAY_MESSAGE_HASHMAP_PREFIX_KEY + topic, id, msgStr);
//                    return operations.exec();
//                }
//            };
//            List<Boolean> resultList = stringRedisTemplate.execute(sessionCallback);
//            if (resultList.get(0) == true && resultList.get(1) == true) {
//                flag = true;
//            }
            
            // Lua 脚本
            Object delayMessageFlag = stringRedisTemplate.execute(
                    SCRIPT_DELAY_MESSAGE,
                    stringRedisTemplate.getStringSerializer(),
                    stringRedisTemplate.getStringSerializer(),
                    Arrays.asList(Constants.DELAY_MESSAGE_TTL_PREFIX_KEY + topic + "-" + id, 
                            Constants.DELAY_MESSAGE_HASHMAP_PREFIX_KEY + topic),
                    expireTime, timeStr,// TTL
                    id, msgStr// hashmap,id作为hashmap的key
                    );
            flag = Objects.equals(delayMessageFlag, DELAY_MESSAGE_SUCCESS);
            
            if (flag) {
                logger.info("RedisMqProducer.syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
                return true;
            } else {
                logger.info("RedisMqProducer.syncDelaySend.fail->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
            }
        } catch (Exception e) {
            logger.error("RedisMqProducer.syncDelaySend.error->topic=[" + topic + "],message=[" + str + "],delayTime=[" + delayTime + "],timeUnit=[" + timeUnit + "]", e);
            e.printStackTrace();
        }
        return false;
    }

}
