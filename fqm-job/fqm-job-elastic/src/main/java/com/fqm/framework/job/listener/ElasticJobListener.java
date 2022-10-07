package com.fqm.framework.job.listener;

import java.lang.reflect.Method;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import com.fqm.framework.job.core.JobContext;

/**
 * ElasticJob任务监听
 * 
 * @version 
 * @author 傅泉明
 */
public class ElasticJobListener extends JobListenerAdapter<JobContext> implements SimpleJob {

    public ElasticJobListener(Object bean, Method method) {
        super(bean, method);
    }
    
    @Override
    public void execute(ShardingContext shardingContext) {
        JobContext jobContext = new JobContext(shardingContext.getTaskId(),
                shardingContext.getJobParameter(), shardingContext.getShardingItem(), shardingContext.getShardingTotalCount());
        super.receiveJob(jobContext);
    }
    
}
