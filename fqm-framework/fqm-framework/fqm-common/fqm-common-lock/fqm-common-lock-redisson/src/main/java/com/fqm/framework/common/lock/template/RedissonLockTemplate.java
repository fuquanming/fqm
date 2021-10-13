package com.fqm.framework.common.lock.template;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import com.fqm.framework.common.lock.impl.RedissonLock;

/**
 * Redisson锁模板
 * 
 * @version 
 * @author 傅泉明
 */
public class RedissonLockTemplate implements LockTemplate<RedissonLock> {

    private final RedissonClient redissonClient;
    
    public RedissonLockTemplate(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public RedissonLock getLock(String key) {
        RLock lock = redissonClient.getLock(key);
        return new RedissonLock(lock);
    }

}
