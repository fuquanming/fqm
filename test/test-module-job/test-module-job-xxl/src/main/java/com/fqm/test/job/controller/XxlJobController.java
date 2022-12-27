package com.fqm.test.job.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.job.annotation.JobListener;
import com.fqm.framework.job.core.JobContext;

@RestController
public class XxlJobController {

    public Logger logger = LoggerFactory.getLogger(getClass());
    /** 任务名称：对应配置文件 job.jobs.xxx */
    public static final String JOB_CREATE_ORDER = "xjob";
    
    @JobListener(name = JOB_CREATE_ORDER)
    public void xjob(JobContext jobContext) {
        logger.info("XxlJobParam=" + jobContext.getJobParam());
        // 分片参数
        int shardIndex = jobContext.getShardIndex();
        int shardTotal = jobContext.getShardTotal();
        logger.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
    }
    
}
