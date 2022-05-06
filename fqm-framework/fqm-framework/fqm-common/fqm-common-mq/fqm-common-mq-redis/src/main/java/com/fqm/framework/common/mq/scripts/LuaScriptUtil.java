package com.fqm.framework.common.mq.scripts;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.fqm.framework.common.mq.redis.PendingMessage;
import com.fqm.framework.common.mq.redis.PendingMessages;
import com.fqm.framework.common.mq.redis.PendingMessagesSummary;
import com.fqm.framework.common.mq.redis.StreamInfo;
import com.fqm.framework.common.mq.redis.StreamInfo.XInfoGroups;

import cn.hutool.extra.spring.SpringUtil;

/**
 * Lua脚本
 * 
 * @version 
 * @author 傅泉明
 */
@SuppressWarnings("rawtypes")
public class LuaScriptUtil {
    /** 获取消费者组 */
    public static final RedisScript<List> SCRIPT_GROUP = new DefaultRedisScript<>("return redis.call('xinfo', 'groups', KEYS[1])", List.class);
    /** 获取消费者组未ack消息的汇总信息 */
    public static final RedisScript<List> SCRIPT_PENDING_GROUP = new DefaultRedisScript<>("local msg=redis.call('xpending', KEYS[1], ARGV[1]) return msg", List.class);
    /** 获取消费者组里消费者未ack消息的摘要 */
    public static final RedisScript<List> SCRIPT_PENDING_GROUP_CONSUMER = new DefaultRedisScript<>("local msg=redis.call('xpending', KEYS[1], ARGV[1], ARGV[2], ARGV[3], ARGV[4], ARGV[5]) return msg", List.class);
    
    @SuppressWarnings("unchecked")
    /**
     * 获取主题里的消费者组
     * @param stringRedisTemplate
     * @param topic
     * @return
     */
    public static XInfoGroups getXInfoGroups(String topic) {
        StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
        List<Object> parts = stringRedisTemplate.execute(SCRIPT_GROUP, Collections.singletonList(topic));
        
        List<Object> result = new ArrayList<>();
        for (List<Object> part : (List<List<Object>>) (Object) parts) {
            Map<String, Object> res = new HashMap<>();
            res.put("name", part.get(1));
            res.put("consumers", part.get(3));
            res.put("pending", part.get(5));
            res.put("last-delivered-id", part.get(7));
            List<Object> list = res.entrySet().stream()
                    .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            result.add(list);
        }
        return StreamInfo.XInfoGroups.fromList(result);
    }
    
    /**
     * 获取消费者组未ack消息的汇总信息
     * @param topic 主题
     * @param group 消费者组
     * @return
     */
    @SuppressWarnings("unchecked")
    public static PendingMessagesSummary pending(String topic, String group) {
        StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
        List<?> parts = stringRedisTemplate.execute(SCRIPT_PENDING_GROUP, Collections.singletonList(topic), group);
        if (parts.isEmpty()) {
            return null;
        }
        PendingMessagesSummary pendingMessagesSummary = null;
        List<List<String>> customerParts = (List<List<String>>) parts.get(3);
        if (customerParts.isEmpty()) {
            pendingMessagesSummary = new PendingMessagesSummary(group, 0, Range.unbounded(), Collections.emptyMap());
        } else {
            Map<String, Long> map = customerParts.stream().collect(Collectors.toMap(e -> e.get(0), e -> Long.valueOf(e.get(1)),
                    (u, v) -> { throw new IllegalStateException("Duplicate key: " + u); },
                    LinkedHashMap::new));
            Range<String> range = Range.open(parts.get(1).toString(), parts.get(2).toString());
            pendingMessagesSummary = new PendingMessagesSummary(group, (Long) parts.get(0), range, map);
        }
        return pendingMessagesSummary;
    }
    
    /**
     * 获取消费者组里消费者未ack消息的摘要
     * @param topic     主题
     * @param group     消费者组
     * @param consumer  消费者
     * @param range     消息ID范围
     * @param count     返回记录数
     * @return
     */
    public static PendingMessages pending(String topic, String group, String consumer, Range<?> range, long count) {
        StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
        List<?> parts = stringRedisTemplate.execute(
                SCRIPT_PENDING_GROUP_CONSUMER,
                Collections.singletonList(topic),
                group, range.getLowerBound().getValue().get(), range.getUpperBound().getValue().get(), String.valueOf(count), consumer
                );
        int size = parts.size();
        List<PendingMessage> pms = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            List params = (List) parts.get(i);
            PendingMessage pm = new PendingMessage(RecordId.of(params.get(0).toString()),
                    Consumer.from(group, params.get(1).toString()),
                    Duration.of(Long.valueOf(params.get(2).toString()), ChronoUnit.MILLIS),
                    Long.valueOf(params.get(3).toString()));
            pms.add(pm);
        }
        return new PendingMessages(group, range, pms);
    }
}
