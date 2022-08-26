package com.fqm.framework.mq.config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
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

/**
 * Kafka消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class KafkaMqAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public KafkaMqTemplate kafkaMqTemplate(MqFactory mqFactory, KafkaTemplate<?, ?> kafkaTemplate) {
        KafkaMqTemplate kafkaMqTemplate = new KafkaMqTemplate(kafkaTemplate);
        mqFactory.addMqTemplate(kafkaMqTemplate);
        return kafkaMqTemplate;
    }
    
    @Resource
    ConsumerFactory<?, ?> consumerFactory;
    @Resource
    MqListenerAnnotationBeanPostProcessor mq;
    @Resource
    KafkaOperations<?, ?> template;
    @Resource
    ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        int i = 0;
        for (MqListenerParam v : mq.getListeners()) {
            String name = "kafkaListener." + i;
            // 动态注册
            //将applicationContext转换为ConfigurableApplicationContext
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            // 获取bean工厂并转换为DefaultListableBeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            if (applicationContext.containsBean(name) == false) {

                if (MqMode.kafka.name().equals(v.getBinder())) {
                    // 通过BeanDefinitionBuilder创建bean定义
                    ContainerProperties containerProperties = new ContainerProperties(v.getTopic());
                    containerProperties.setGroupId(v.getGroup());
                    containerProperties.setAckMode(AckMode.MANUAL);// 手动ack
                    containerProperties.setMessageListener(new KafkaMqListener(v.getBean(), v.getMethod()));

                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(KafkaMessageListenerContainer.class);
//                            .genericBeanDefinition(ConcurrentMessageListenerContainer.class);
                    beanDefinitionBuilder.addConstructorArgValue(consumerFactory);
                    beanDefinitionBuilder.addConstructorArgValue(containerProperties);
                    // 失败重试1次，最后入死信队列，topic=v.getDestination() + ".DLT"
                    beanDefinitionBuilder.addPropertyValue("errorHandler", 
                            new SeekToCurrentErrorHandler(new DeadLetterPublishingRecoverer(template), 
                            new FixedBackOff(10 * 1000L, 1L)));
//                    // 并发消费
//                    beanDefinitionBuilder.addPropertyValue("concurrency", v.getConcurrentConsumers());
                    
                    // 注册bean
                    defaultListableBeanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getRawBeanDefinition());
                }
            }
            i++;
        }
    }

}
