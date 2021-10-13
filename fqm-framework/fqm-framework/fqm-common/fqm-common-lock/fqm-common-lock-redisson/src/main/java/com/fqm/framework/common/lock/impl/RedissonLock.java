package com.fqm.framework.common.lock.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;

import com.fqm.framework.common.lock.Lock;

/**
 * Redisson的锁
 * 
 * @version 
 * @author 傅泉明
 */
public class RedissonLock implements Lock {

    RLock lock;
    
    public RedissonLock(RLock lock) {
        this.lock = lock;
    }
    
    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        try {
            return lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (lock.isHeldByCurrentThread()) {
            try {
                return lock.forceUnlockAsync().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
