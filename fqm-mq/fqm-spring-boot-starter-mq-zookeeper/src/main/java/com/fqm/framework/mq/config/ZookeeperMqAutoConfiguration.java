package com.fqm.framework.mq.config;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
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

import com.fqm.framework.common.zookeeper.ZookeeperFactory;
import com.fqm.framework.common.zookeeper.ZookeeperProperties;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
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
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class ZookeeperMqAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.cloud.zookeeper")
    ZookeeperProperties zookeeperProperties() {
        return new ZookeeperProperties();
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(CuratorFramework.class)
    @Order(200)
    public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
        return ZookeeperFactory.buildCuratorFramework(zookeeperProperties);
    }

    @Bean(destroyMethod = "destroy")
    @ConditionalOnMissingBean
    @Order(200)
    ZookeeperMqTemplate zookeeperMqTemplate(MqFactory mqFactory, CuratorFramework curatorFramework) {
        ZookeeperMqTemplate zookeeperMqTemplate = new ZookeeperMqTemplate(curatorFramework);
        mqFactory.addMqTemplate(zookeeperMqTemplate);
        return zookeeperMqTemplate;
    }

    @Override
    public void afterSingletonsInstantiated() {
        MqListenerAnnotationBeanPostProcessor mq = applicationContext.getBean(MqListenerAnnotationBeanPostProcessor.class);
        List<MqListenerParam> listenerParams = mq.getListeners(MqMode.ZOOKEEPER);
        if (null == listenerParams || listenerParams.isEmpty()) {
            return;
        }
        
        ZookeeperMqTemplate zookeeperMqTemplate = applicationContext.getBean(ZookeeperMqTemplate.class);
        for (MqListenerParam v : listenerParams) {
            MqListener mqListener = v.getMqListener();
            // 1、解析@MqListener
            String topic = mqListener.topic();
            zookeeperMqTemplate.getQueue(topic, new ZookeeperMqListener(v.getBean(), v.getMethod(), zookeeperMqTemplate, topic));
            logger.info("Init ZookeeperMqListener,bean={},method={},topic={}", v.getBean().getClass(), v.getMethod().getName(), topic);
        }
    }
}
