package com.fqm.framework.locks.impl;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import com.fqm.framework.locks.Lock;
/**
 * Zookeeper锁
 * 
 * @version 
 * @author 傅泉明
 */
public class ZookeeperLock implements Lock {

    InterProcessMutex interProcessMutex;
    
    public ZookeeperLock(InterProcessMutex interProcessMutex) {
        this.interProcessMutex = interProcessMutex;
    }

    @Override
    public void lock() {
        try {
            interProcessMutex.acquire();
        } catch (Exception e) {
            throw new RuntimeException("zookeeperLock lock fail", e);
        }
    }

    @Override
    public boolean tryLock() {
        try {
            return interProcessMutex.acquire(0, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("zookeeperLock tryLock fail", e);
        }
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        try {
            return interProcessMutex.acquire(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("zookeeperLock tryLock fail", e);
        }
    }

    @Override
    public boolean unlock() {
        try {
            interProcessMutex.release();
        } catch (Exception e) {
            throw new RuntimeException("zookeeperLock unlock fail", e);
        }
        return false;
    }

}
