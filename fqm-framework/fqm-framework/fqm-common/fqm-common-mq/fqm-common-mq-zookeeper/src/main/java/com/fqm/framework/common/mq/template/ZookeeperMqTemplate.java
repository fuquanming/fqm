package com.fqm.framework.common.mq.template;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.common.lock.impl.SimpleLock;
import com.fqm.framework.common.lock.template.SimpleLockTemplate;

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

    private Map<String, DistributedQueue<String>> queueMap = new ConcurrentHashMap<>();
    
    private SimpleLockTemplate simpleLockTemplate = new SimpleLockTemplate();

    public ZookeeperMqTemplate(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = JsonUtil.toJsonStr(msg);
        try {
            DistributedQueue<String> queue = getQueue(topic, null);
            queue.put(str);
            logger.info("ZookeeperMqProducer.success->topic=[{}],message=[{}]", topic, str);
        } catch (Exception e) {
            logger.error("ZookeeperMqProducer.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }

    public DistributedQueue<String> getQueue(String topic, QueueConsumer<String> consumer) {
        DistributedQueue<String> queue = queueMap.get(topic);
        if (queue == null) {
            SimpleLock lock = simpleLockTemplate.getLock(topic);;
            lock.lock();
            try {
                queue = queueMap.get(topic);
                if (queue != null) return queue;
                
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
                queue = queueBuilder.buildQueue();
                try {
                    queue.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                queueMap.put(topic, queue);
            } finally {
                lock.unlock();
            }
        }
        return queue;
    }

    public void destroy() {
        for (Entry<String, DistributedQueue<String>> entry : queueMap.entrySet()) {
            String topic = entry.getKey();
            DistributedQueue<String> queue = entry.getValue();
            try {
                logger.info("destroy topic:[{}]", topic);
                queue.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
