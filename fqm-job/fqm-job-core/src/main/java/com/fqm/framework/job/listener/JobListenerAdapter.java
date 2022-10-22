/*
 * @(#)JobListenerAdapter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-job-core
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.listener;

import java.lang.reflect.Method;

import com.fqm.framework.job.exception.InvokeException;

/**
 * 任务监听适配器
 * @version 
 * @author 傅泉明
 */
public class JobListenerAdapter<T> implements JobListener<T> {

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
    
    public JobListenerAdapter(Object bean, Method method) {
        this.method = method;
        this.bean = bean;
    }
    
    @Override
    public void receiveJob(T message) {
        try {
            if (method.getParameterCount() == 1) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes[0] == message.getClass()) {
                    method.invoke(bean, message);
                }
            } else {
                method.invoke(bean);
            }
        } catch (Exception e) {
            throw new InvokeException(e);
        }
    }
    
}
