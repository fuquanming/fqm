package com.fqm.framework.locks.template;

import java.util.HashMap;
import java.util.Map;

import com.fqm.framework.locks.LockMode;
import com.fqm.framework.locks.impl.SimpleLock;

/**
 * 基于本地内存的锁
 * 
 * @version 
 * @author 傅泉明
 */
public class SimpleLockTemplate implements LockTemplate<SimpleLock> {
    
    private Map<String, SimpleLock> lockMap = new HashMap<>();

    @Override
    public synchronized SimpleLock getLock(String key) {
        return lockMap.computeIfAbsent(key, k -> new SimpleLock());
    }
    
    @Override
    public LockMode getLockMode() {
        return LockMode.SIMPLE;
    }
    
}
