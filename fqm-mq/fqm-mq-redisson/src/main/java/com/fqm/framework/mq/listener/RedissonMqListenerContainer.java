package com.fqm.framework.mq.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
        if (size <= Runtime.getRuntime().availableProcessors()) {
            size = Runtime.getRuntime().availableProcessors() * 2;
        }
        pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 
                size,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = defaultFactory.newThread(r);
                        if (!thread.isDaemon()) {
                            thread.setDaemon(true);
                        }
                        thread.setName("mq-redisson-" + threadNumber.getAndIncrement());
                        return thread;
                    }
                });
    }
    
    public void stop() {
        listeners.forEach(RedissonMqListener::stop);
        pool.shutdown();
    }
    
    public void register(RedissonMqListener redissonMqListener) {
        listeners.add(redissonMqListener);
        pool.execute(redissonMqListener);
    }
    
}
