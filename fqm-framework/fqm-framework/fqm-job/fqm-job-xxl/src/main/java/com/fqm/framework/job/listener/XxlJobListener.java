package com.fqm.framework.job.listener;

import java.lang.reflect.Method;

import com.fqm.framework.job.core.JobContext;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.impl.MethodJobHandler;

/**
 * xxl任务监听
 * 
 * @version 
 * @author 傅泉明
 */
public class XxlJobListener extends JobListenerAdapter<JobContext> {

    public XxlJobListener(Object bean, Method method, String jobName) {
        super(bean, method);
        try {
            // 注册本身的 receiveJob 方法
            XxlJobExecutor.registJobHandler(jobName, 
                    new MethodJobHandler(this, this.getClass().getMethod("receiveJob", JobContext.class), null, null));
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveJob(JobContext jJobContext) throws Exception {
        // 初始化
        XxlJobContext context = XxlJobContext.getXxlJobContext();
        super.receiveJob(new JobContext(String.valueOf(context.getJobId()), context.getJobParam(), context.getShardIndex(), context.getShardTotal()));
    }

}
