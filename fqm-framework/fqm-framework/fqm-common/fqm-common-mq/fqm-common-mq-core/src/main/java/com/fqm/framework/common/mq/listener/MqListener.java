package com.fqm.framework.common.mq.listener;

/**
 * 监听消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public interface MqListener<T> {
    /**
     * 监听到json对象
     * @param message
     * @throws Exception
     */
    public void receiveMessage(T message) throws Exception;
    
}
