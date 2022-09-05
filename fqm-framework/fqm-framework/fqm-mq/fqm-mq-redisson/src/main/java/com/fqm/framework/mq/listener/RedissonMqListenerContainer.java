package com.fqm.framework.mq.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * Redisson消息队列监听器容器
 * 
 * @version 
 * @author 傅泉明
 */
public class RedissonMqListenerContainer {

    private ExecutorService pool;
    
    private List<RedissonMqListener> listeners = new ArrayList<>();
    
    public RedissonMqListenerContainer(int size) {
        pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 
                size,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }
    
    public void stop() {
        listeners.forEach(listener -> {
            listener.stop();
        });
        pool.shutdown();
    }
    
    public void register(RedissonMqListener redissonMqListener) {
        listeners.add(redissonMqListener);
        pool.execute(redissonMqListener);
    }
    
}
