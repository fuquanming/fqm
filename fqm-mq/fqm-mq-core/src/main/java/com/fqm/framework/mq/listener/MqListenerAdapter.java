package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;

/**
 * 监听消息队列适配器
 * 
 * @version 
 * @author 傅泉明
 */
public class MqListenerAdapter<T> implements MqListener<T> {
    
    private Method method;
    private Object bean;
    
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
    
    public MqListenerAdapter(Method method, Object bean) {
        this.method = method;
        this.bean = bean;
    }

    @Override
    public void receiveMessage(T message) throws Exception {
        method.invoke(bean, message);
    }
}
