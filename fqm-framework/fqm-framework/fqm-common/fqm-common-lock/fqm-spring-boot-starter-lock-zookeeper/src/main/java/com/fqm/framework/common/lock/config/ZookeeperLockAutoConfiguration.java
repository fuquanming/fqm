package com.fqm.framework.common.lock.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.lock.LockFactory;
import com.fqm.framework.common.lock.condition.ZookeeperCondition;
import com.fqm.framework.common.lock.template.ZookeeperLockTemplate;

/**
 * Redisson 锁自动装配
 * 使用spring.cloud.zookeeper配置文件
 * zookeeper建立连接采用异步操作，连接操作后，并不能保证zk连接已成功。
 * connectionTimeout 默认15秒，加大该值
 * 如果zookeeper连接成功之前访问zookeeper，会出现错误：
 * org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
// 检查配置文件
@Conditional(ZookeeperCondition.class)
@ConfigurationProperties(prefix = "spring.cloud.zookeeper")
public class ZookeeperLockAutoConfiguration {
    /** zookeeper服务端 */
    private String connectString;
    /** 会话超时时间 */
    private int sessionTimeout = 30000;
    /** 连接超时时间 */
    private int connectionTimeout = 15000;
    /** 初始休眠时间 */
    private int baseSleepTimeMs = 1000;
    /** 最大重试次数 */
    private int maxRetries = 3;
    /** 最大休眠时间 */
    private int maxSleepMs = 10000;
    
//    /** 获取连接阻塞时超时时间单位 */
//    private TimeUnit blockUntilConnectedUnit; 
//    /** 获取连接阻塞时超时时间 */
//    private int blockUntilConnectedWait = 10000;
    
//    connect-string: 
//    connection-timeout: 
//    session-timeout:
    
//    block-until-connected-unit: 
//    block-until-connected-wait: 
//    enabled:
    
//    base-sleep-time-ms: 
//    max-retries: 
//    max-sleep-ms: 

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework curatorFramework() {
        if (this.connectionTimeout < 15000) this.connectionTimeout = 15000;// 必须大于15秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(this.baseSleepTimeMs, this.maxRetries, this.maxSleepMs);
        return CuratorFrameworkFactory.builder()
                .connectString(this.connectString)
                .sessionTimeoutMs(this.sessionTimeout)
                .connectionTimeoutMs(this.connectionTimeout)
                .retryPolicy(retryPolicy)
                .build();
    }
    
    @Bean
    @ConditionalOnMissingBean
    @Order(300)
    public ZookeeperLockTemplate zookeeperLockTemplate(
            LockFactory lockFactory,
            CuratorFramework curatorFramework) {
        ZookeeperLockTemplate zookeeperLockTemplate = new ZookeeperLockTemplate(curatorFramework);
        lockFactory.addLockTemplate(zookeeperLockTemplate);
        return zookeeperLockTemplate;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxSleepMs() {
        return maxSleepMs;
    }

    public void setMaxSleepMs(int maxSleepMs) {
        this.maxSleepMs = maxSleepMs;
    }
}
