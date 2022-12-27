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
 * 任务监听器参数
 * @version 
 * @author 傅泉明
 */
public class JobListenerParam {
    /**
     * JobProperties 属性mqs（Map）里的Key
     */
    private String name;
    /**
     * 使用 @JobListener 监听的Bean 
     */
    private Object bean;
    /**
     * 使用 @JobListener 监听的Bean的方法
     */
    private Method method;
    /**
     * JobProperties 属性mqs（Map）里的Key
     */
    public String getName() {
        return name;
    }
    public JobListenerParam setName(String name) {
        this.name = name;
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
