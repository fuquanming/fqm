package com.fqm.framework.locks.template;

import com.fqm.framework.locks.Lock;
import com.fqm.framework.locks.LockMode;

/**
 * 锁模板
 * 
 * @version 
 * @author 傅泉明
 */
public interface LockTemplate<T extends Lock> {

    /**
     * 获取锁
     * @param key
     * @return
     */
    public T getLock(String key);
    /**
     * 获取锁的方式
     * @return
     */
    public LockMode getLockMode();
    
}
