package com.fqm.framework.locks.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.fqm.framework.locks.Lock;

/**
 * JDK的锁
 * 
 * @version 
 * @author 傅泉明
 */
public class SimpleLock implements Lock {

    private ReentrantLock lock = new ReentrantLock();
    
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
        lock.unlock();
        return true;
    }

}
