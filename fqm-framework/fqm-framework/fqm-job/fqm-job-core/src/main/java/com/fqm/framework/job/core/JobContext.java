package com.fqm.framework.job.core;
/**
 * 任务上下文
 * 
 * @version 
 * @author 傅泉明
 */
public class JobContext {

    /**
     * 任务ID
     */
    private final String jobId;

    /**
     * 任务参数
     */
    private final String jobParam;

    // ---------------------- for shard ----------------------

    /**
     * 分片索引，0开始
     */
    private final int shardIndex;

    /**
     * 分片总数
     */
    private final int shardTotal;

    public JobContext(String jobId, String jobParam, int shardIndex, int shardTotal) {
        this.jobId = jobId;
        this.jobParam = jobParam;
        this.shardIndex = shardIndex;
        this.shardTotal = shardTotal;
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobParam() {
        return jobParam;
    }

    public int getShardIndex() {
        return shardIndex;
    }

    public int getShardTotal() {
        return shardTotal;
    }

}
