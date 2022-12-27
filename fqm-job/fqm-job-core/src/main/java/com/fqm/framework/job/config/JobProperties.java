/*
 * @(#)JobProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-job-core
 * 创建日期 : 2022年9月6日
 * 修改历史 : 
 *     1. [2022年9月6日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Job properties
 * @version 
 * @author 傅泉明
 */
public class JobProperties {
    
    /** 是否开启，默认为 false 未开启 */
    private Boolean enabled = false;
    
    /** 任务配置 */
    private Map<String, JobConfigurationProperties> jobs = new LinkedHashMap<>();
    
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, JobConfigurationProperties> getJobs() {
        return jobs;
    }

    public void setJobs(Map<String, JobConfigurationProperties> jobs) {
        this.jobs = jobs;
    }
}
