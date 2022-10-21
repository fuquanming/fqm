package com.fqm.framework.mq.template;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.locks.impl.SimpleLock;
import com.fqm.framework.locks.template.SimpleLockTemplate;
import com.fqm.framework.mq.MqMode;

import io.netty.util.CharsetUtil;

/**
 * Zookeeper消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public class ZookeeperMqTemplate implements MqTemplate {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private CuratorFramework curatorFramework;

    private Map<String, DistributedDelayQueue<String>> queueMap = new ConcurrentHashMap<>();
    
    private SimpleLockTemplate simpleLockTemplate = new SimpleLockTemplate();

    public ZookeeperMqTemplate(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }
    
    @Override
    public MqMode getMqMode() {
        return MqMode.zookeeper;
    }

    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = JsonUtil.toJsonStr(msg);
        try {
            DistributedDelayQueue<String> queue = getQueue(topic, null);
            queue.put(str, System.currentTimeMillis());
            logger.info("ZookeeperMqProducer.syncSend.success->topic=[{}],message=[{}]", topic, str);
            return true;
        } catch (Exception e) {
            logger.error("ZookeeperMqProducer.syncSend.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean syncDelaySend(String topic, Object msg, int delayTime, TimeUnit timeUnit) {
        String str = JsonUtil.toJsonStr(msg);
        try {
            DistributedDelayQueue<String> queue = getQueue(topic, null);
            queue.put(str, System.currentTimeMillis() + timeUnit.toMillis(delayTime));
            logger.info("ZookeeperMqProducer.syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
            return true;
        } catch (Exception e) {
            logger.error("ZookeeperMqProducer.syncDelaySend.error->topic=[" + topic + "],message=[" + str + "],delayTime=[" + delayTime + "],timeUnit=[" + timeUnit + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 
     * @param topic
     * @param consumer  topic的监听器
     * @return
     */
    public DistributedDelayQueue<String> getQueue(String topic, QueueConsumer<String> consumer) {
        DistributedDelayQueue<String> queue = queueMap.get(topic);
        if (queue == null) {
            SimpleLock lock = simpleLockTemplate.getLock(topic);
            lock.lock();
            try {
                queue = queueMap.get(topic);
                if (queue != null) {
                    return queue;
                }
                
                QueueBuilder<String> queueBuilder = QueueBuilder.builder(curatorFramework, 
                        consumer, new QueueSerializer<String>() {
                    @Override
                    public byte[] serialize(String item) {
                        return item.getBytes(CharsetUtil.UTF_8);
                    }

                    @Override
                    public String deserialize(byte[] bytes) {
                        return new String(bytes, CharsetUtil.UTF_8);
                    }
                }, "/mq/" + topic);
                queue = queueBuilder.buildDelayQueue();
                try {
                    queue.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                queueMap.put(topic, queue);
                logger.info("InIt Zookeeper Listener,{}", "/mq/" + topic);
            } finally {
                lock.unlock();
            }
        }
        return queue;
    }

    public void destroy() {
        for (Entry<String, DistributedDelayQueue<String>> entry : queueMap.entrySet()) {
            String topic = entry.getKey();
            DistributedDelayQueue<String> queue = entry.getValue();
            try {
                logger.info("destroy topic:[{}]", topic);
                queue.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
