package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;
/**
 * 消息队列监听参数
 * 
 * @version 
 * @author 傅泉明
 */
public class MqListenerParam {
    /**
     * MqProperties 属性mqs（Map）里的Key，即消息主题topic
     */
    private String name;
    /**
     * 使用 @MqListener 监听的Bean 
     */
    private Object bean;
    /**
     * 使用 @MqListener 监听的Bean的方法
     */
    private Method method;
    
    /**
     * MqProperties 属性mqs（Map）里的Key
     */
    public String getName() {
        return name;
    }
    public MqListenerParam setName(String name) {
        this.name = name;
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
