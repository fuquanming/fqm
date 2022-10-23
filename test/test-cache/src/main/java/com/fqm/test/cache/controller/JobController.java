package com.fqm.test.cache.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.job.annotation.JobListener;
import com.fqm.framework.job.core.JobContext;

@RestController
public class JobController {

    public Logger logger = LoggerFactory.getLogger(getClass());

    @JobListener(name = "${job.jobs.xjob.name:}")
    public void xjob(JobContext jobContext) {
        logger.info("XxlJobParam=" + jobContext.getJobParam());
        // 分片参数
        int shardIndex = jobContext.getShardIndex();
        int shardTotal = jobContext.getShardTotal();
        logger.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
    }
    
    @JobListener(name = "${job.jobs.ejob.name:}")
    public void ejob(JobContext jobContext) {
        logger.info("ElasticJobParam=" + jobContext.getJobParam());
        // 分片参数
        int shardIndex = jobContext.getShardIndex();
        int shardTotal = jobContext.getShardTotal();
        logger.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
    }
}
