package com.fqm.framework.common.mq.config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.mq.MqFactory;
import com.fqm.framework.common.mq.MqMode;
import com.fqm.framework.common.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.common.mq.listener.MqListenerParam;
import com.fqm.framework.common.mq.listener.ZookeeperMqListener;
import com.fqm.framework.common.mq.template.ZookeeperMqTemplate;

/**
 * Zookeeper消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConfigurationProperties(prefix = "spring.cloud.zookeeper")
public class ZookeeperMqAutoConfiguration {

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

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework curatorFramework() {
        if (this.connectionTimeout < 15000) this.connectionTimeout = 15000;// 必须大于15秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(this.baseSleepTimeMs, this.maxRetries, this.maxSleepMs);
        return CuratorFrameworkFactory.builder().connectString(this.connectString).sessionTimeoutMs(this.sessionTimeout)
                .connectionTimeoutMs(this.connectionTimeout).retryPolicy(retryPolicy).build();
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnMissingBean
    @Order(200)
    public ZookeeperMqTemplate zookeeperMqTemplate(MqFactory mqFactory, CuratorFramework curatorFramework) {
        ZookeeperMqTemplate zookeeperMqTemplate = new ZookeeperMqTemplate(curatorFramework);
        mqFactory.addMqTemplate(zookeeperMqTemplate);
        return zookeeperMqTemplate;
    }

    @Resource
    MqListenerAnnotationBeanPostProcessor mq;

    @Resource
    CuratorFramework curatorFramework;
    
    @Resource
    ZookeeperMqTemplate zookeeperMqTemplate;

    @PostConstruct
    public void init() {
        for (MqListenerParam v : mq.getListeners()) {
            if (MqMode.zookeeper.name().equals(v.getBinder())) {
                DistributedQueue<String> queueDead = zookeeperMqTemplate.getQueue(v.getDestination() + ".DLQ", null);
                // 死信队列放入监听队列中
                zookeeperMqTemplate.getQueue(v.getDestination(), new ZookeeperMqListener(v.getBean(), v.getMethod(), queueDead));
            }
        }
    }
}
