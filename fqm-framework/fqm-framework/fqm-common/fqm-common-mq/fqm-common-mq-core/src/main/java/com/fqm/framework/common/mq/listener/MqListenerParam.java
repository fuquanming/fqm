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
    public void setBinder(String binder) {
        this.binder = binder;
    }
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }
    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }
    public Object getBean() {
        return bean;
    }
    public void setBean(Object bean) {
        this.bean = bean;
    }
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }

}
