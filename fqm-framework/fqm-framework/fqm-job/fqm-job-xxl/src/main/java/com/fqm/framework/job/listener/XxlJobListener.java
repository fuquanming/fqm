package com.fqm.framework.job.listener;

import java.lang.reflect.Method;

import com.fqm.framework.job.core.JobContext;
import com.xxl.job.core.context.XxlJobContext;

/**
 * xxl任务监听
 * 
 * @version 
 * @author 傅泉明
 */
public class XxlJobListener extends JobListenerAdapter<JobContext> {

    public XxlJobListener(Object bean, Method method) {
        super(bean, method);
    }

    @Override
    public void receiveJob(JobContext jJobContext) {
        // 初始化
        XxlJobContext context = XxlJobContext.getXxlJobContext();
        super.receiveJob(new JobContext(String.valueOf(context.getJobId()), context.getJobParam(), context.getShardIndex(), context.getShardTotal()));
    }

}
