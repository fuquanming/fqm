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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fqm.framework.job.JobMode;

/**
 * Job properties
 * @version 
 * @author 傅泉明
 */
public class JobProperties {
    
    /** 记录加载的job */
    public static Set<JobMode> JOB_MODE_MAP = new HashSet<>();
    
    /** 是否开启，默认为 false 未开启 */
    private Boolean enabled = false;
    /** 校验加载的任务组件，默认为 true */
    private Boolean verify = true;
    /** 任务方式，指定所有任务的实现方式 */
    private JobMode binder;
    /** 任务配置，key：任务名称，value：任务配置 */
    private Map<String, JobConfigurationProperties> jobs = new LinkedHashMap<>();
    
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Boolean getVerify() {
        return verify;
    }

    public void setVerify(Boolean verify) {
        this.verify = verify;
    }

    public JobMode getBinder() {
        return binder;
    }

    public void setBinder(JobMode binder) {
        this.binder = binder;
    }
    
    public Map<String, JobConfigurationProperties> getJobs() {
        return jobs;
    }

    public void setJobs(Map<String, JobConfigurationProperties> jobs) {
        this.jobs = jobs;
    }
}
