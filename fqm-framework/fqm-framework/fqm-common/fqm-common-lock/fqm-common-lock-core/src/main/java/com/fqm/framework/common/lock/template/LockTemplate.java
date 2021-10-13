package com.fqm.framework.common.lock.template;

import com.fqm.framework.common.lock.Lock;

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
    
}
