package com.fqm.framework.common.mq;
/**
 * 消息队列的方式，和消息队列模板一一对应
 * 
 * @version 
 * @author 傅泉明
 */
public enum MqMode {
    
    kafka,
    rabbit,
    redis,
    rocket,
    zookeeper;
    
    public static MqMode getLockMode(String mode) {
        if (mode == null || "".equals(mode)) {
            return null;
        }
        MqMode[] lockModes = values();
        for (MqMode lockMode : lockModes) {
            if (mode.equals(lockMode.name())) {
                return lockMode;
            }
        }
        return null;
    }
    
}
