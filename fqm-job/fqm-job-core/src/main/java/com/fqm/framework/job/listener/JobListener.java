/*
 * @(#)JobListener.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-job-core
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.listener;

/**
 * 监听任务
 * @version 
 * @author 傅泉明
 */
public interface JobListener<T> {
    
    /**
     * 监听到json对象
     * @param message
     * @throws Exception
     */
    public void receiveJob(T message);

}
