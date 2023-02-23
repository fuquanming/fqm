package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;

import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
/**
 * 消息队列监听参数
 * 
 * @version 
 * @author 傅泉明
 */
public class MqListenerParam {
    /**
     * 消息监听注解
     */
    private MqListener mqListener;
    /**
     * 消息监听使用的消息方式
     */
    private MqMode mqMode;
    /**
     * 使用 @MqListener 监听的Bean 
     */
    private Object bean;
    /**
     * 使用 @MqListener 监听的Bean的方法
     */
    private Method method;
    
    public MqListener getMqListener() {
        return mqListener;
    }
    public MqListenerParam setMqListener(MqListener mqListener) {
        this.mqListener = mqListener;
        return this;
    }
    public MqMode getMqMode() {
        return mqMode;
    }
    public MqListenerParam setMqMode(MqMode mqMode) {
        this.mqMode = mqMode;
        return this;
    }
    public Object getBean() {
        return bean;
    }
    public MqListenerParam setBean(Object bean) {
        this.bean = bean;
        return this;
    }
    public Method getMethod() {
        return method;
    }
    public MqListenerParam setMethod(Method method) {
        this.method = method;
        return this;
    }

}
