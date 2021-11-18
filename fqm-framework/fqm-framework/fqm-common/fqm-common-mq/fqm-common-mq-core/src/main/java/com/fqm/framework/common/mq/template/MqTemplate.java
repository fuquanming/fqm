package com.fqm.framework.common.mq.template;

/**
 * 消息队列模板
 * 
 * @version 
 * @author 傅泉明
 */
public interface MqTemplate {

    /**
     * 同步发送消息，对象使用json保存的队列中
     * @param topic 主题
     * @param msg   消息
     * @return
     */
    public boolean syncSend(String topic, Object msg);
    
}
