package com.fqm.framework.mq.tasker;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.fqm.framework.mq.constant.Constants;
import com.fqm.framework.mq.redis.PendingMessage;
import com.fqm.framework.mq.redis.PendingMessages;
import com.fqm.framework.mq.redis.PendingMessagesSummary;
import com.fqm.framework.mq.redis.StreamInfo.InfoGroup;
import com.fqm.framework.mq.redis.StreamInfo.InfoGroups;
import com.fqm.framework.mq.scripts.LuaScriptUtil;

/**
 * Redis死信队列任务
 * 1、将未ack的消息手动ack
 * 2、将未ack的消息放入死信队列中，队列命名:消费者组名.DLQ
 * @version 
 * @author 傅泉明
 */
public class RedisMqDeadMessageTasker {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private ScheduledExecutorService timer;
    private StringRedisTemplate stringRedisTemplate;
    private static final RedisScript<String> SCRIPT_DEAD_MESSAGE = 
            new DefaultRedisScript<>(
                    // ACK消息
                    "local ack = redis.call('xack', KEYS[1], ARGV[1], ARGV[2]) " +   
                    "if ack==1 then" + 
                    "    return redis.call('xadd', KEYS[2], 'MAXLEN', '~', ARGV[3], '*', 'deadMessage', ARGV[4]) " +    // 投递死信消息到队列
                    "else " + 
                    "    return 0 " +
                    "end"
                    , String.class
                    );
    /** 监听的主题 */
    private Set<String> topics;
    /** 判断为死信：消息消费次数 */
    private long deadMessageDeliveryCount;
    /** 判断为死信：消息消费时间单位秒 */
    private long deadMessageDeliverySecond;
    /**
     * 
     * @param stringRedisTemplate
     * @param topics
     * @param deadMessageDeliveryCount
     * @param deadMessageDeliverySecond
     */
    public RedisMqDeadMessageTasker(StringRedisTemplate stringRedisTemplate, Set<String> topics, long deadMessageDeliveryCount, long deadMessageDeliverySecond) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.topics = topics;
        this.deadMessageDeliveryCount = deadMessageDeliveryCount <= 0 ? 1 : deadMessageDeliveryCount;
        this.deadMessageDeliverySecond = deadMessageDeliverySecond <= 0 ? 60 : deadMessageDeliveryCount;
    }
    
    public void start() {
        if (timer == null) {
            timer = new ScheduledThreadPoolExecutor(2, new ThreadFactory() {
                private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = defaultFactory.newThread(r);
                    if (!thread.isDaemon()) {
                        thread.setDaemon(true);
                    }
                    thread.setName("mq-redis-deadMsg-" + threadNumber.getAndIncrement());
                    return thread;
                }
            });
        }
        // 1分钟执行一次
        timer.scheduleWithFixedDelay(new DeadMessageTasker(stringRedisTemplate, topics, deadMessageDeliveryCount, deadMessageDeliverySecond), 1, 1, TimeUnit.MINUTES);
    }
    
    public void stop() {
        if (timer != null) {
            timer.shutdown();
        }
    }
    
    private static class DeadMessageTasker extends TimerTask {
        
        private Logger logger = LoggerFactory.getLogger(getClass());
        
        private StringRedisTemplate stringRedisTemplate;
        /** 监听的主题 */
        private Set<String> topics;
        /** 判断为死信：消息消费次数 */
        private long deadMessageDeliveryCount;
        /** 判断为死信：消息消费时间单位秒 */
        private long deadMessageDeliverySecond;
        
        public DeadMessageTasker(StringRedisTemplate stringRedisTemplate, Set<String> topics, long deadMessageDeliveryCount, long deadMessageDeliverySecond) {
            this.stringRedisTemplate = stringRedisTemplate;
            this.topics = topics;
            this.deadMessageDeliveryCount = deadMessageDeliveryCount <= 0 ? 1 : deadMessageDeliveryCount;
            this.deadMessageDeliverySecond = deadMessageDeliverySecond <= 0 ? 60 : deadMessageDeliveryCount;
        }
        @Override
        public void run() {
            try {
                StreamOperations<String, String, String> streamOperations = stringRedisTemplate.opsForStream();
                for (String topic : topics) {
                    // 获取消费者组 
                    // 替换XInfoGroups groups = streamOperations.groups(topic); 需要spring-boot->2.4.2,redisson-spring-boot-starter->3.15.1
                    InfoGroups groups = LuaScriptUtil.getInfoGroups(topic, stringRedisTemplate);
                    if (groups == null) {
                        continue;
                    }
                    for (Iterator<InfoGroup> it = groups.iterator(); it.hasNext();) {
                        InfoGroup group = it.next();
                        Long pendingCount = group.pendingCount();// 未ack的消息
                        if (pendingCount.longValue() > 0) {
                            String groupName = group.groupName();
                            // 死信队列topic
                            String deadTopic = groupName + ".DLQ";
                            // 获取消费者组里的pending消息
                            PendingMessagesSummary pendingMessagesSummary = LuaScriptUtil.pending(topic, groupName, stringRedisTemplate);
                            // 每个消费者的pending消息数量
                            Map<String, Long> pendingMessagesPerConsumer = pendingMessagesSummary.getPendingMessagesPerConsumer();
                            pendingMessagesPerConsumer.entrySet().forEach(entry -> {
                                // 消费者
                                String consumer = entry.getKey();
                                // 消费者的pending消息数量
                                long consumerTotalPendingMessages = entry.getValue();
                                if (consumerTotalPendingMessages > 0) {
                                    // 读取消费者pending队列的前10条记录，从ID=0的记录开始，一直到ID最大值，一次处理10条
                                    PendingMessages pendingMessages = LuaScriptUtil.pending(topic, groupName, consumer, Range.closed("0", "+"), 10, stringRedisTemplate);
                                    // 遍历所有Opending消息的详情
                                    pendingMessages.forEach(message -> 
                                        // 消息的ID
                                        executeDeadMessage(streamOperations, topic, groupName, deadTopic, message)
                                    );
                                }
                            });
                            
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void executeDeadMessage(StreamOperations<String, String, String> streamOperations, String topic, String groupName, String deadTopic,
                PendingMessage message) {
            String flag = "-";
            String messageId = message.getIdAsString();
            // 消息投递一次消费的时间，每投递一次重置该时间
            Duration elapsedTimeSinceLastDelivery = message.getElapsedTimeSinceLastDelivery();
            // 在多个消费者中投递的次数
            long deliveryCount = message.getTotalDeliveryCount();
            String consumerName = message.getConsumerName();
            // 是否死信消息，投递次数大于1或消费时间大于60秒
            if (deliveryCount > deadMessageDeliveryCount || elapsedTimeSinceLastDelivery.getSeconds() > deadMessageDeliverySecond) {
                // 获取消息内容
                List<MapRecord<String, String, String>> result = streamOperations
                        .range(topic, Range.rightOpen(messageId, messageId));
                if (result != null && !result.isEmpty()) {
                    MapRecord<String, String, String> mapRecord = result.get(0);
                    logger.info("deadMessage->topic={},group={},consumer={},id={},deliveryCount={},deliveryTimer={}", 
                            topic, groupName, consumerName, messageId, deliveryCount, elapsedTimeSinceLastDelivery.getSeconds());
                    String msg = mapRecord.getValue().values().iterator().next();
                    // ack并放入死信队列
                    // Lua
                    Object deadMessageFlag = stringRedisTemplate.execute(
                            SCRIPT_DEAD_MESSAGE,
                            stringRedisTemplate.getStringSerializer(),
                            stringRedisTemplate.getStringSerializer(),
                            Arrays.asList(topic, deadTopic),
                            groupName, messageId,
                            String.valueOf(Constants.MAX_QUEUE_SIZE), msg
                            );
                    if (deadMessageFlag.toString().contains(flag)) {
                        logger.info("deadMessage.ok->topic:{},id={}", deadTopic, deadMessageFlag);
                    }
                }
            }
        }
    }
}
