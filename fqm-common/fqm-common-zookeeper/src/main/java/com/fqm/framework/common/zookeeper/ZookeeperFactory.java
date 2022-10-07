package com.fqm.framework.common.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
/**
 * 构建Zookeeper客户端
 * 
 * @version 
 * @author 傅泉明
 */
public class ZookeeperFactory {
    /**
     * 获取Zookeeper客户端 
     * @param config
     * @return
     */
    public static CuratorFramework buildCuratorFramework(ZookeeperConfig config) {
        if (config.getConnectionTimeout() < 15000) config.setConnectionTimeout(15000);// 必须大于15秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(config.getBaseSleepTimeMs(), config.getMaxRetries(), config.getMaxSleepMs());
        return CuratorFrameworkFactory.builder().connectString(config.getConnectString()).sessionTimeoutMs(config.getSessionTimeout())
                .connectionTimeoutMs(config.getConnectionTimeout()).retryPolicy(retryPolicy).build();
    }

}
