package com.fqm.framework.mq.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.KafkaMqListener;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.template.KafkaMqTemplate;
import com.google.common.base.Preconditions;

/**
 * Kafka消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class KafkaMqAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    } 
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public KafkaMqTemplate kafkaMqTemplate(MqFactory mqFactory, KafkaTemplate<?, ?> kafkaTemplate) {
        KafkaMqTemplate kafkaMqTemplate = new KafkaMqTemplate(kafkaTemplate);
        mqFactory.addMqTemplate(kafkaMqTemplate);
        return kafkaMqTemplate;
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        MqListenerAnnotationBeanPostProcessor mq = applicationContext.getBean(MqListenerAnnotationBeanPostProcessor.class);
        MqProperties mp = applicationContext.getBean(MqProperties.class);
        
        ConsumerFactory<?, ?> consumerFactory = applicationContext.getBean(ConsumerFactory.class);
        KafkaOperations<?, ?> template = applicationContext.getBean(KafkaOperations.class);
        int i = 0;
        for (MqListenerParam v : mq.getListeners()) {
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties == null) {
                properties = getProperties(mp, name, properties);
            }
            if (properties != null && MqMode.KAFKA.name().equalsIgnoreCase(properties.getBinder())) {
                String group = properties.getGroup();
                String topic = properties.getTopic();
                Preconditions.checkArgument(StringUtils.isNotBlank(group), "Please specific [group] under mq configuration.");
                Preconditions.checkArgument(StringUtils.isNotBlank(topic), "Please specific [topic] under mq configuration.");
                String beanName = "kafkaListener." + i;
                // 动态注册
                //将applicationContext转换为ConfigurableApplicationContext
                ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
                // 获取bean工厂并转换为DefaultListableBeanFactory
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
                if (!applicationContext.containsBean(beanName)) {
                    // 通过BeanDefinitionBuilder创建bean定义
                    ContainerProperties containerProperties = new ContainerProperties(topic);
                    containerProperties.setGroupId(group);
                    // 手动ack
                    containerProperties.setAckMode(AckMode.MANUAL);
                    containerProperties.setMessageListener(new KafkaMqListener(v.getBean(), v.getMethod()));

                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(KafkaMessageListenerContainer.class);
                                /** 并发消费 genericBeanDefinition(ConcurrentMessageListenerContainer.class);*/
                    beanDefinitionBuilder.addConstructorArgValue(consumerFactory);
                    beanDefinitionBuilder.addConstructorArgValue(containerProperties);
                    // 失败重试1次，最后入死信队列，topic=v.getDestination() + ".DLT"
                    beanDefinitionBuilder.addPropertyValue("errorHandler", 
                            new SeekToCurrentErrorHandler(new DeadLetterPublishingRecoverer(template), 
                            new FixedBackOff(10 * 1000L, 1L)));
                    /** 并发消费 addPropertyValue("concurrency", v.getConcurrentConsumers()); */
                    // 注册bean
                    defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
                    i++;
                    logger.info("Init KafkaMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
                }
            }
        }
    }

    private MqConfigurationProperties getProperties(MqProperties mp, String name, MqConfigurationProperties properties) {
        // 遍历mp.mqs
        for (MqConfigurationProperties mcp : mp.getMqs().values()) {
            if (mcp.getName().equals(name) && MqMode.KAFKA.name().equalsIgnoreCase(mcp.getBinder())) {
                properties = mcp;
                break;
            }
        }
        return properties;
    }
}
