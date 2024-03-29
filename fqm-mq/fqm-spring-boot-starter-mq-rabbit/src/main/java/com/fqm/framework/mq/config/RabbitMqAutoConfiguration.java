package com.fqm.framework.mq.config;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.spring.util.SpringUtil;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.RabbitMqListener;
import com.fqm.framework.mq.template.RabbitMqTemplate;

/**
 * Rabbit消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class RabbitMqAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    } 
    
    @Bean(destroyMethod = "destroy")
    @ConditionalOnMissingBean
    @Order(300)
    RabbitMqTemplate rabbitMqTemplate(MqFactory mqFactory, RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin, RabbitProperties rabbitProperties) {
        // Rabbitmq控台访问端口 
        String apiPortStr = SpringUtil.getProperty("spring.rabbitmq.api-port");
        int apiPort = 15672;
        if (StringUtils.isNoneBlank(apiPortStr)) {
            apiPort = Integer.parseInt(apiPortStr);
        }
        RabbitMqTemplate rabbitMqTemplate = new RabbitMqTemplate(rabbitTemplate, amqpAdmin, rabbitProperties, apiPort);
        mqFactory.addMqTemplate(rabbitMqTemplate);
        return rabbitMqTemplate;
    }
    
    @Bean
    @ConditionalOnMissingBean
    RabbitPropertiesBeanPostProcessor rabbitPropertiesBeanPostProcessor() {
        return new RabbitPropertiesBeanPostProcessor();
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        MqListenerAnnotationBeanPostProcessor mq = applicationContext.getBean(MqListenerAnnotationBeanPostProcessor.class);
        // 初始化监听器
        List<MqListenerParam> listenerParams = mq.getListeners(MqMode.RABBIT);
        if (null == listenerParams || listenerParams.isEmpty()) {
            return;
        }
        
        RabbitMqTemplate rabbitMqTemplate = applicationContext.getBean(RabbitMqTemplate.class);
        ConnectionFactory connectionFactory = applicationContext.getBean(ConnectionFactory.class);
        int i = 0;
        for (MqListenerParam v : listenerParams) {
            MqListener mqListener = v.getMqListener();
            // 1、解析@MqListener
            String topic = mqListener.topic();
            String group = mqListener.group();
            String beanName = "rabbitListener." + i;
            // 动态注册
            //将applicationContext转换为ConfigurableApplicationContext
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            // 获取bean工厂并转换为DefaultListableBeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            if (!applicationContext.containsBean(beanName)) {
                String queueName = rabbitMqTemplate.initTopic(topic, group);
                
                // 通过BeanDefinitionBuilder创建bean定义
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                        .genericBeanDefinition(SimpleMessageListenerContainer.class);
                beanDefinitionBuilder.addConstructorArgValue(connectionFactory);
                beanDefinitionBuilder.addPropertyValue("messageListener", new RabbitMqListener(v.getBean(), v.getMethod()));
                beanDefinitionBuilder.addPropertyValue("queues", new Queue(queueName));
                //设置当前的消费者数量
                beanDefinitionBuilder.addPropertyValue("concurrentConsumers", 1);
                //设置手动签收
                beanDefinitionBuilder.addPropertyValue("acknowledgeMode", AcknowledgeMode.MANUAL);
//                        beanDefinitionBuilder.addPropertyValue("transactionManager", new RabbitTransactionManager(connectionFactory));//设置事务
                // 注册bean
                defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getRawBeanDefinition());
                i++;
                logger.info("Init RabbitMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
            }
        }
    }

}
