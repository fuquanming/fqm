/*
 * @(#)JobListenerParam.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-job-core
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.listener;

import java.lang.reflect.Method;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class JobListenerParam {

    private String name;
    private String binder;
    private Object bean;
    private Method method;
    
    public String getName() {
        return name;
    }
    public JobListenerParam setName(String name) {
        this.name = name;
        return this;
    }
    public String getBinder() {
        return binder;
    }
    public JobListenerParam setBinder(String binder) {
        this.binder = binder;
        return this;
    }
    public Object getBean() {
        return bean;
    }
    public JobListenerParam setBean(Object bean) {
        this.bean = bean;
        return this;
    }
    public Method getMethod() {
        return method;
    }
    public JobListenerParam setMethod(Method method) {
        this.method = method;
        return this;
    }
    
}
