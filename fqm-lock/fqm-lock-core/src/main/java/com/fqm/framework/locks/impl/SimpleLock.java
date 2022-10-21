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

    private ReentrantLock reentrantLock = new ReentrantLock();
    
    @Override
    public void lock() {
        reentrantLock.lock();
    }

    @Override
    public boolean tryLock() {
        return reentrantLock.tryLock();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        try {
            return reentrantLock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean unlock() {
        reentrantLock.unlock();
        return true;
    }

}
