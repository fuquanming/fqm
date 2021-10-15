package com.fqm.framework.common.lock.impl;

import java.lang.management.ManagementFactory;
import java.sql.Blob;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.fqm.framework.common.lock.Lock;
import com.fqm.framework.common.lock.LockFactory;
import com.fqm.framework.common.lock.redis.listener.spring.LockRedisKeyDeleteEventHandle;
import com.fqm.framework.common.lock.template.LockTemplate;
import com.fqm.framework.common.lock.template.SimpleLockTemplate;

/**
 * RedisTemplate的锁
 * 1.获取不到锁，则阻塞该线程（超时时间为锁过期时间）
 * 2.监听被删除的锁（过期也会被触发），唤醒一个本地JVM记录阻塞的线程{@link LockRedisKeyDeleteEventHandle}
 * 3.唤醒的线程从阻塞线程集合中移除自己
 * 任务执行时间大于锁的过期时间
 * 
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisTemplateLock implements Lock {

    private static Logger logger = LoggerFactory.getLogger(RedisTemplateLock.class);
    
    private static final RedisScript<String> SCRIPT_LOCK = new DefaultRedisScript<>("return redis.call('set',KEYS[1]," + "ARGV[1],'NX','PX',ARGV[2])",
            String.class);

    private static final RedisScript<String> SCRIPT_UNLOCK = new DefaultRedisScript<>(
            "if redis.call('get',KEYS[1]) " + "== ARGV[1] then return tostring(redis.call('del', KEYS[1])==1) else return 'false' end", String.class);

    private static final String LOCK_SUCCESS = "OK";
    
    /** JVM阻塞的线程,key:lockKey, value:Set<Thread> */
//    public static Set<Thread> BLOCK_THREADS = ConcurrentHashMap.newKeySet();
    public static Map<String, Set<Thread>> BLOCK_THREADS = new ConcurrentHashMap<>();
    /** Redis锁操作 */
    private final StringRedisTemplate stringRedisTemplate;

    private final String lockKey;

    private String lockValue;
    /** 使用JVM锁 */
    private LockFactory lockFactory;
    
    public RedisTemplateLock(StringRedisTemplate stringRedisTemplate, String lockKey, LockFactory lockFactory) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString().replaceAll("-", "");
        this.lockFactory = lockFactory;
    }
    
    /**
     * 获取JVM的name
     * @return jvmPid@主机名
     */
    public String getJvmName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }
    
    @Override
    public void lock() {
        while (true) {
            String lock = stringRedisTemplate.execute(
                    SCRIPT_LOCK,
                    stringRedisTemplate.getStringSerializer(),
                    stringRedisTemplate.getStringSerializer(),
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(3000));
            boolean locked = LOCK_SUCCESS.equals(lock);
            if (locked) {
                // 记录当前持有锁的线程
                break;
            } else {
                // 获取锁的剩余时间，并阻塞该线程到指定时间
                Long expireMillisecond = stringRedisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);// 过期时间毫秒
//                String redisLockValue = stringRedisTemplate.opsForValue().get(lockKey); // 锁的值，当过期，锁被删除时，阻塞的线程自动给他续期时间
                long nanos = TimeUnit.MILLISECONDS.toNanos(expireMillisecond);
                
                // 初始化阻塞线程集合
                LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(SimpleLockTemplate.class);
                Lock lockKeyLock = lockTemplate.getLock(lockKey);
                lockKeyLock.lock();
                Set<Thread> threads = BLOCK_THREADS.get(lockKey);
                try {
                    if (threads == null) {
                        threads = ConcurrentHashMap.newKeySet();
                        BLOCK_THREADS.put(lockKey, threads);
                        logger.info("new block threads,lockKey->{}", lockKey);
                    }
                } finally {
                    lockKeyLock.unlock();
                }
                threads.add(Thread.currentThread());
                // 阻塞线程
                LockSupport.parkNanos(this, nanos);
                // 移除阻塞线程
                BLOCK_THREADS.get(lockKey).remove(Thread.currentThread());
                // 删除事件会唤醒阻塞线程，或超时自动唤醒阻塞线程
                logger.info("retry lock ->" + Thread.currentThread());
                // 判断原锁任务是否过期
                
            }
        }
    }

    @Override
    public boolean tryLock() {
        String lock = stringRedisTemplate.execute(
                SCRIPT_LOCK,
                stringRedisTemplate.getStringSerializer(),
                stringRedisTemplate.getStringSerializer(),
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(30000));
        boolean locked = LOCK_SUCCESS.equals(lock);
        return locked;
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public boolean unlock() {
        String releaseResult = stringRedisTemplate.execute(
                SCRIPT_UNLOCK,
                stringRedisTemplate.getStringSerializer(),
                stringRedisTemplate.getStringSerializer(),
                Collections.singletonList(lockKey),
                lockValue);
        return Boolean.parseBoolean(releaseResult);
    }

}
