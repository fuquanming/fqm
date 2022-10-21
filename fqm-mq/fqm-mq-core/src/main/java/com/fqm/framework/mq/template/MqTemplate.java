package com.fqm.framework.mq.template;

import java.util.concurrent.TimeUnit;

import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.client.producer.SendCallback;

/**
 * 消息队列模板
 * 
 * @version 
 * @author 傅泉明
 */
public interface MqTemplate {
    /**
     * 序列号对象为json
     * @param msg
     * @return
     */
    public default String getJsonStr(Object msg) {
        return JsonUtil.toJsonStr(msg);
    }
    /**
     * 获取消息存储的方式
     * @return
     */
    public MqMode getMqMode();
    
    /**
     * 同步发送消息，对象使用json保存到队列中
     * @param topic 主题
     * @param msg   消息
     * @return
     */
    public boolean syncSend(String topic, Object msg);
    
    /**
     * 同步发送延迟消息，对象使用json保存到队列中
     * @param topic         主题
     * @param msg           消息
     * @param delayTime     延迟时间
     * @param timeUnit      延时时间单位
     * @return
     */
    public default boolean syncDelaySend(String topic, Object msg, int delayTime, TimeUnit timeUnit) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, "未实现同步延迟消息发送");
    }
    /**
     * 异步发送消息，对象使用json保存到队列中 
     * @param topic
     * @param msg
     * @param sendCallback
     */
    public default void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        throw new com.fqm.framework.common.core.exception.ServiceException(11, "未实现异步消息发送");
    }
    
}
