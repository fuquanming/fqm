package com.fqm.framework.common.lock.template;

import java.util.HashMap;
import java.util.Map;

import com.fqm.framework.common.lock.impl.SimpleLock;

/**
 * 基于本地内存的锁
 * 
 * @version 
 * @author 傅泉明
 */
public class SimpleLockTemplate implements LockTemplate<SimpleLock> {
    
    private Map<String, SimpleLock> lockMap = new HashMap<String, SimpleLock>();

    @Override
    public synchronized SimpleLock getLock(String key) {
        SimpleLock cacheLock = lockMap.get(key);
        if (cacheLock == null) {
            cacheLock = new SimpleLock();
            lockMap.put(key, cacheLock);
        }
        return cacheLock;
    }
    
}
