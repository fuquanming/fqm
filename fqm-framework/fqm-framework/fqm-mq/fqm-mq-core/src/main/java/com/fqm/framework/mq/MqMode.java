package com.fqm.framework.mq;
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
    redisson,
    rocket,
    zookeeper;
    
    public static MqMode getMode(String mode) {
        if (mode == null || "".equals(mode)) {
            return null;
        }
        MqMode[] modes = values();
        for (MqMode m : modes) {
            if (mode.equals(m.name())) {
                return m;
            }
        }
        return null;
    }
    
}

