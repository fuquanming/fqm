package com.fqm.framework.job.config;

import com.fqm.framework.job.JobMode;

/**
 * 任务自动注册配置
 * 
 * @version 
 * @author 傅泉明
 */
public class JobConfigurationProperties {
    /** 执行任务的组件名 参考@JobMode,必填 */
    private JobMode binder;
    /** 执行任务的时间,elasticjob必填 */
    private String cron;
    
    public JobMode getBinder() {
        return binder;
    }
    public void setBinder(JobMode binder) {
        this.binder = binder;
    }
    public String getCron() {
        return cron;
    }
    public void setCron(String cron) {
        this.cron = cron;
    }
}
