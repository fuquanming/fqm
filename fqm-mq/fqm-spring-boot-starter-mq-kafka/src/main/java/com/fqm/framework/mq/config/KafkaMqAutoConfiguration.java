package com.fqm.framework.mq.config;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.BiConsumer;

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
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.KafkaMqListener;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.template.KafkaMqTemplate;

/**
 * Kafka消息队列自动装配
 * 
 * @version 1.0.3
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class KafkaMqAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;
    /** spring-boot-dependencies 2.7.2,高版本类 */
    private static final String ERROR_HANDLER_HIGHTER_CLASS = "org.springframework.kafka.listener.DefaultErrorHandler";
    /** spring-boot-dependencies 2.4.2,低版本类，高版本中被废弃 */
    private static final String ERROR_HANDLER_LOW_CLASS = "org.springframework.kafka.listener.SeekToCurrentErrorHandler";
    /** errorHandler 构造方法 */
    private Constructor<?> errorHandlerConstructor = null; 
    
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        errorHandlerConstructor = getErrorHandlerClassConstructor();
    } 
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    KafkaMqTemplate kafkaMqTemplate(MqFactory mqFactory, KafkaTemplate<?, ?> kafkaTemplate) {
        KafkaMqTemplate kafkaMqTemplate = new KafkaMqTemplate(kafkaTemplate);
        mqFactory.addMqTemplate(kafkaMqTemplate);
        return kafkaMqTemplate;
    }
    
    /**
     * 获取 ErrorHandlerClass 构造方法
     * @return Constructor
     */
    private Constructor<?> getErrorHandlerClassConstructor() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            // 有高版本使用
            Class<?> errorHandlerClass = ClassUtils.forName(ERROR_HANDLER_HIGHTER_CLASS, classLoader);
            return errorHandlerClass.getConstructor(
                    ClassUtils.forName("org.springframework.kafka.listener.ConsumerRecordRecoverer", classLoader), 
                    BackOff.class);
        } catch (Exception e) {
            // 使用低版本
            try {
                Class<?> errorHandlerClass = ClassUtils.forName(ERROR_HANDLER_LOW_CLASS, classLoader);
                return errorHandlerClass.getConstructor(BiConsumer.class, BackOff.class);
            } catch (Exception e1) {
                // DoNothing
            }
        }
        return null;
    }
    
    private Object getErrorHandlerObj(KafkaOperations<?, ?> template) {
        try {
            return errorHandlerConstructor.newInstance(new DeadLetterPublishingRecoverer(template), new FixedBackOff(10 * 1000L, 1L));
        } catch (Exception e) {
            // DoNothing
        }
        return null;
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        MqListenerAnnotationBeanPostProcessor mq = applicationContext.getBean(MqListenerAnnotationBeanPostProcessor.class);
        List<MqListenerParam> listenerParams = mq.getListeners(MqMode.KAFKA);
        if (null == listenerParams || listenerParams.isEmpty()) {
            return;
        }
        
        ConsumerFactory<?, ?> consumerFactory = applicationContext.getBean(ConsumerFactory.class);
        KafkaOperations<?, ?> template = applicationContext.getBean(KafkaOperations.class);
        int i = 0;
        
        for (MqListenerParam v : listenerParams) {
            MqListener mqListener = v.getMqListener();
            String topic = mqListener.topic();
            String group = mqListener.group();
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

                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(KafkaMessageListenerContainer.class);
                // 并发消费的类 "ConcurrentMessageListenerContainer"
                beanDefinitionBuilder.addConstructorArgValue(consumerFactory);
                beanDefinitionBuilder.addConstructorArgValue(containerProperties);
                // 失败重试1次，最后入死信队列，topic=v.getDestination() + ".DLT"
                Object errorHandlerObj = getErrorHandlerObj(template);
                if (ERROR_HANDLER_HIGHTER_CLASS.equals(errorHandlerConstructor.getName())) {
                    beanDefinitionBuilder.addPropertyValue("commonErrorHandler", errorHandlerObj);
                } else if (ERROR_HANDLER_LOW_CLASS.equals(errorHandlerConstructor.getName())) {
                    beanDefinitionBuilder.addPropertyValue("errorHandler", errorHandlerObj);
                }
                // 并发消费 "concurrency"
                // 注册bean
                defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
                i++;
                logger.info("Init KafkaMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
            }
        }
    }

}
