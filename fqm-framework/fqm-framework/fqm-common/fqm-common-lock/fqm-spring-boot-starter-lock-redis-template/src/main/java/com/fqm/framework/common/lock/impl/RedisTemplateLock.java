package com.fqm.framework.common.lock.impl;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.fqm.framework.common.lock.Lock;
import com.fqm.framework.common.lock.LockFactory;
import com.fqm.framework.common.lock.executor.BlockLockThreadExecutor;
import com.fqm.framework.common.lock.redis.listener.spring.LockRedisKeyDeleteEventHandle;
import com.fqm.framework.common.lock.template.LockTemplate;
import com.fqm.framework.common.lock.template.SimpleLockTemplate;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * RedisTemplate的锁
 * 1.获取不到锁，则阻塞该线程（超时时间为锁过期时间）
 * 2.监听被删除的锁（过期也会被触发），唤醒一个本地JVM记录阻塞的线程{@link LockRedisKeyDeleteEventHandle}
 * 3.唤醒的线程从阻塞线程集合中移除自己
 * 任务执行时间大于锁的过期时间
 * 1.获得锁的线程，自己开启续订锁的到期时间
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
    /** 获得锁成功的值 */
    private static final String LOCK_SUCCESS = "OK";
    
    /** JVM阻塞的线程,key:lockKey, value:Set<Thread> */
    public static Map<String, Set<BlockLockThreadExecutor>> BLOCK_THREADS = new ConcurrentHashMap<>();
    /** Redis锁操作 */
    private final StringRedisTemplate stringRedisTemplate;

    private final String lockKey;

    private final String lockValue;
    /** 锁超时时间，单位：毫秒 */
    private static final long LOCK_KEY_TIMEOUT = 3000;
    
    /** 使用JVM锁 */
    private LockFactory lockFactory;
    /** 尝试获取锁时间，单位：纳秒 */
    private long tryLockTimeout;
    
    private BlockLockThreadExecutor lockBlockThreadExecutor;
    
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
    
    /**
     * 初始化阻塞线程集合
     */
    private void initBlockThreads() {
        // 初始化阻塞线程集合
        LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(SimpleLockTemplate.class);
        Lock lockKeyLock = lockTemplate.getLock(lockKey);
        lockKeyLock.lock();
        Set<BlockLockThreadExecutor> threads = BLOCK_THREADS.get(lockKey);
        try {
            if (threads == null) {
                threads = ConcurrentHashMap.newKeySet();
                BLOCK_THREADS.put(lockKey, threads);
            }
            if (lockBlockThreadExecutor == null) {
                lockBlockThreadExecutor = new BlockLockThreadExecutor().setBlockThread(Thread.currentThread()).setDeleteNotify(false);
            }
        } finally {
            lockKeyLock.unlock();
        }
        threads.add(lockBlockThreadExecutor);
    }
    
    private boolean lockKey(String lockValue) {
        String lock = stringRedisTemplate.execute(
                SCRIPT_LOCK,
                stringRedisTemplate.getStringSerializer(),
                stringRedisTemplate.getStringSerializer(),
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(LOCK_KEY_TIMEOUT));
        return LOCK_SUCCESS.equals(lock);
    }
    
    @Override
    public void lock() {
        while (true) {
            boolean locked = tryLock();
            if (locked) {
                // 开启定时任务，判断任务超时
                renewExpiration();
                break;
            } else {
                // 获取锁的剩余时间，并阻塞该线程到指定时间
                Long expireMillisecond = stringRedisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);// 过期时间毫秒
                long nanos = TimeUnit.MILLISECONDS.toNanos(expireMillisecond);
                
                initBlockThreads();
                
                // 阻塞线程
                LockSupport.parkNanos(this, nanos);
                // 移除阻塞线程
                BLOCK_THREADS.get(lockKey).remove(lockBlockThreadExecutor);
                // 删除事件会唤醒阻塞线程，或超时自动唤醒阻塞线程
                logger.debug("retry lock ->" + Thread.currentThread());
            }
        }
    }

    @Override
    public boolean tryLock() {
        return lockKey(lockValue);
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        while (true) {
            boolean locked = tryLock();
            if (locked) {
                renewExpiration();
                return true;
            } else {
                // 尝试锁时间
                tryLockTimeout = unit.toNanos(timeout);
                
                initBlockThreads();
                
                long beginTime = System.nanoTime();
                
                // 阻塞线程
                LockSupport.parkNanos(this, tryLockTimeout);
                // 移除阻塞线程
                BLOCK_THREADS.get(lockKey).remove(lockBlockThreadExecutor);
                
                // 判断尝试锁是否超时
                long now = System.nanoTime();
                // 阻塞耗时
                long blockTime = now - beginTime;
                
                tryLockTimeout = tryLockTimeout - blockTime;
                if (tryLockTimeout <= 0) {// 已尝试完成
                    return false;
                }
                // 删除事件会唤醒阻塞线程，或超时自动唤醒阻塞线程
                logger.debug("retry tryLock ->" + Thread.currentThread());
                
            }
        }
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
    /** 续订到期时间 */
    private void renewExpiration() {
        getHashedWheelTimer().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                String value = stringRedisTemplate.opsForValue().get(lockKey);
                if (value != null) {
                    boolean flag = stringRedisTemplate.expire(lockKey, LOCK_KEY_TIMEOUT, TimeUnit.MILLISECONDS);
                    logger.debug("renew lockKey->{},{}", lockKey, flag);
                    renewExpiration();
                }
            }
        }, LOCK_KEY_TIMEOUT / 3, TimeUnit.MILLISECONDS);
    }
    /** 延时任务 */
    public static class HashedWheelTimerInstance {
        
        public static HashedWheelTimer instance = null;
        
        static {
            instance = new HashedWheelTimer(Executors.defaultThreadFactory(), 100, TimeUnit.MILLISECONDS, 32);
        }
        
    }
    
    private static HashedWheelTimer getHashedWheelTimer() {
        return HashedWheelTimerInstance.instance;
    }
    
}
