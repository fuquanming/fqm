/*
 * @(#)JobVerification.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job
 * 创建日期 : 2023年4月4日
 * 修改历史 : 
 *     1. [2023年4月4日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.Assert;

import com.fqm.framework.job.JobMode;

/**
 * job校验
 * 1、校验配置文件
 * 2、判断配置文件中是否加载任务组件
 * job.binder 或者 job.jobs.xxx.binder
 * @version 
 * @author 傅泉明
 */
public class JobVerification implements SmartInitializingSingleton {

    private JobProperties jobProperties;

    public JobVerification(JobProperties jobProperties) {
        this.jobProperties = jobProperties;
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        // 1、校验配置文件
        JobMode jobMode = jobProperties.getBinder();
        jobProperties.getJobs().entrySet().forEach(entry -> {
            String businessName = entry.getKey();
            JobConfigurationProperties jcp = entry.getValue();

            JobMode binder = jcp.getBinder();
            if (null == binder) {
                binder = jobMode;
            }
            Assert.notNull(binder, "Please specific [binder] under [job.jobs." + businessName + "] configuration or [binder] under [job] configuration.");
            // 校验 JobMode 
            Assert.isTrue(JobProperties.JOB_MODE_MAP.contains(binder), "Please specific [binder] under [job.jobs." + businessName + "] configuration, not found [" + binder + "].");
        });
    }

}
