package com.fqm.framework.locks;
/**
 * 锁的方式，和锁模板一一对应
 * 
 * @version 
 * @author 傅泉明
 */
public enum LockMode {
    /** 单机内存锁 */
    SIMPLE,
    /** 分布式Redisson */
    REDISSON,
    /** 分布式Redis */
    REDIS,
    /** 分布式Zookeeper */
    ZOOKEEPER;
    
    public static LockMode getMode(String mode) {
        if (mode == null || "".equals(mode)) {
            return null;
        }
        String modeName = mode;
        LockMode[] modes = values();
        for (LockMode m : modes) {
            if (modeName.toUpperCase().contains(m.name())) {
                return m;
            }
        }
        return null;
    }
    
}
