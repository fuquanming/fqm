package com.fqm.framework.mq.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.ZookeeperMqListener;
import com.fqm.framework.mq.template.ZookeeperMqTemplate;
import com.google.common.base.Preconditions;

/**
 * Zookeeper消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class ZookeeperMqAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;
    private int defaultConnectionTimeout = 15000;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.cloud.zookeeper")
    public ZookeeperProperties zookeeperConfig() {
        return new ZookeeperProperties();
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(CuratorFramework.class)
    public CuratorFramework curatorFramework(ZookeeperProperties zkProperties) {
        if (zkProperties.getConnectionTimeout() < defaultConnectionTimeout) {
            // 必须大于15秒
            zkProperties.setConnectionTimeout(defaultConnectionTimeout);
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(zkProperties.getBaseSleepTimeMs(), 
                zkProperties.getMaxRetries(), zkProperties.getMaxSleepMs());
        return CuratorFrameworkFactory.builder().connectString(zkProperties.getConnectString()).sessionTimeoutMs(zkProperties.getSessionTimeout())
                .connectionTimeoutMs(zkProperties.getConnectionTimeout()).retryPolicy(retryPolicy).build();
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnMissingBean
    @Order(200)
    public ZookeeperMqTemplate zookeeperMqTemplate(MqFactory mqFactory, CuratorFramework curatorFramework) {
        ZookeeperMqTemplate zookeeperMqTemplate = new ZookeeperMqTemplate(curatorFramework);
        mqFactory.addMqTemplate(zookeeperMqTemplate);
        return zookeeperMqTemplate;
    }

    @Override
    public void afterSingletonsInstantiated() {
        MqListenerAnnotationBeanPostProcessor mq = applicationContext.getBean(MqListenerAnnotationBeanPostProcessor.class);
        MqProperties mp = applicationContext.getBean(MqProperties.class);
        
        ZookeeperMqTemplate zookeeperMqTemplate = applicationContext.getBean(ZookeeperMqTemplate.class);
        for (MqListenerParam v : mq.getListeners()) {
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties == null) {
                // 遍历mp.mqs
                for (MqConfigurationProperties mcp : mp.getMqs().values()) {
                    if (mcp.getName().equals(name) && MqMode.ZOOKEEPER.name().equalsIgnoreCase(mcp.getBinder())) {
                        properties = mcp;
                        break;
                    }
                }

            }
            if (properties != null && MqMode.ZOOKEEPER.name().equalsIgnoreCase(properties.getBinder())) {
                String topic = properties.getTopic();
                Preconditions.checkArgument(StringUtils.isNotBlank(topic), "Please specific [topic] under mq configuration.");
                zookeeperMqTemplate.getQueue(topic, new ZookeeperMqListener(v.getBean(), v.getMethod(), zookeeperMqTemplate, topic));
                logger.info("Init ZookeeperMqListener,bean={},method={},topic={}", v.getBean().getClass(), v.getMethod().getName(), topic);
            }
        }
    }
}
