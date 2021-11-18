package com.fqm.framework.common.mq.listener;

import java.lang.reflect.Method;
/**
 * 消息队列监听参数
 * 
 * @version 
 * @author 傅泉明
 */
public class MqListenerParam {
    private String binder;
    private String destination;
    private String group;
    
    private int concurrentConsumers;
    
    private Object bean;
    private Method method;
    
    public String getBinder() {
        return binder;
    }
    public MqListenerParam setBinder(String binder) {
        this.binder = binder;
        return this;
    }
    public String getDestination() {
        return destination;
    }
    public MqListenerParam setDestination(String destination) {
        this.destination = destination;
        return this;
    }
    public String getGroup() {
        return group;
    }
    public MqListenerParam setGroup(String group) {
        this.group = group;
        return this;
    }
    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }
    public MqListenerParam setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
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
