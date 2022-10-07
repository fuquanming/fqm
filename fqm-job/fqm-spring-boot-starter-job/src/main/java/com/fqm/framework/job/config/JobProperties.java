/*
 * @(#)JobProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job
 * 创建日期 : 2022年9月6日
 * 修改历史 : 
 *     1. [2022年9月6日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Job properties
 * @version 
 * @author 傅泉明
 */
@ConfigurationProperties(prefix = "job")
public class JobProperties {
    /** 任务配置 */
    private Map<String, JobConfigurationProperties> jobs = new LinkedHashMap<>();
    
    public Map<String, JobConfigurationProperties> getJobs() {
        return jobs;
    }

    public void setJobs(Map<String, JobConfigurationProperties> jobs) {
        this.jobs = jobs;
    }
}
