package com.fqm.framework.locks;
/**
 * 锁的方式，和锁模板一一对应
 * 
 * @version 
 * @author 傅泉明
 */
public enum LockMode {
    /** 单机内存锁 */
    simple,
    /** 分布式Redisson */
    redisson,
    /** 分布式Redis */
    redis,
    /** 分布式Zookeeper */
    zookeeper;
    
    public static LockMode getLockMode(String mode) {
        if (mode == null || "".equals(mode)) {
            return null;
        }
        String modeName = mode;
        LockMode[] lockModes = values();
        for (LockMode lockMode : lockModes) {
            if (modeName.contains(lockMode.name())) {
                return lockMode;
            }
        }
        return null;
    }
    
}
