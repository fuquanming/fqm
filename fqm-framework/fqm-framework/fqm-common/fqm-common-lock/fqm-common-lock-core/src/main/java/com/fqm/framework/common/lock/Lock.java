package com.fqm.framework.common.lock;

import java.util.concurrent.TimeUnit;
/**
 * 锁
 * 
 * @version 
 * @author 傅泉明
 */
public interface Lock {
    /**
     * 获取锁，获取失败则阻塞该线程 
     */
    void lock();
    /**
     * 尝试获取一次锁
     * @return
     */
    boolean tryLock();
    /**
     * 尝试获取锁
     * @param timeout   获取锁超时时间
     * @param unit      时间单位
     * @return
     */
    boolean tryLock(long timeout, TimeUnit unit);
    /**
     * 释放锁
     * @return
     */
    boolean unlock();
    
}
