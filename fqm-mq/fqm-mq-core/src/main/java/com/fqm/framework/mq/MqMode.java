package com.fqm.framework.mq;
/**
 * 消息队列的方式，和消息队列模板一一对应
 * 
 * @version 
 * @author 傅泉明
 */
public enum MqMode {
    /** kafka */
    KAFKA,
    /** rabbit */
    RABBIT,
    /** redis */
    REDIS,
    /** redisson */
    REDISSON,
    /** rocket */
    ROCKET,
    /** zookeeper */
    ZOOKEEPER,
    /** emqx,mqtt */
    EMQX;
    
}

