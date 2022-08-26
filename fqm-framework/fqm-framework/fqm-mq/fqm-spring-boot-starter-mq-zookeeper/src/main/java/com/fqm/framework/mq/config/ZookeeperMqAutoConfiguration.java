package com.fqm.framework.mq.config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.ZookeeperMqListener;
import com.fqm.framework.mq.template.ZookeeperMqTemplate;

/**
 * Zookeeper消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class ZookeeperMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.cloud.zookeeper")
    public ZookeeperConfig zookeeperConfig() {
        return new ZookeeperConfig();
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework curatorFramework(ZookeeperConfig zkConfig) {
        if (zkConfig.getConnectionTimeout() < 15000) zkConfig.setConnectionTimeout(15000);// 必须大于15秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(zkConfig.getBaseSleepTimeMs(), 
                zkConfig.getMaxRetries(), zkConfig.getMaxSleepMs());
        return CuratorFrameworkFactory.builder().connectString(zkConfig.getConnectString()).sessionTimeoutMs(zkConfig.getSessionTimeout())
                .connectionTimeoutMs(zkConfig.getConnectionTimeout()).retryPolicy(retryPolicy).build();
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
    ZookeeperMqTemplate zookeeperMqTemplate;

    @PostConstruct
    public void init() {
        for (MqListenerParam v : mq.getListeners()) {
            if (MqMode.zookeeper.name().equals(v.getBinder())) {
                zookeeperMqTemplate.getQueue(v.getTopic(), new ZookeeperMqListener(v.getBean(), v.getMethod(), zookeeperMqTemplate, v.getTopic()));
            }
        }
    }
}
