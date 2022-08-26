package com.fqm.framework.mq.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
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
public class RabbitMqAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @Order(300)
    public RabbitMqTemplate rabbitMqTemplate(MqFactory mqFactory, RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        RabbitMqTemplate rabbitMqTemplate = new RabbitMqTemplate(rabbitTemplate, amqpAdmin);
        mqFactory.addMqTemplate(rabbitMqTemplate);
        return rabbitMqTemplate;
    }
    
    @Bean
    public Object rabbitListener(
            MqListenerAnnotationBeanPostProcessor mq, ApplicationContext applicationContext,
            ConnectionFactory connectionFactory, RabbitMqTemplate rabbitMqTemplate) {
        int i = 0;
        for (MqListenerParam v : mq.getListeners()) {
            String name = "rabbitListener." + i;
            // 动态注册
            //将applicationContext转换为ConfigurableApplicationContext
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            // 获取bean工厂并转换为DefaultListableBeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            if (!applicationContext.containsBean(name)) {
                if (MqMode.rabbit.name().equals(v.getBinder())) {
                    rabbitMqTemplate.initTopic(v.getTopic(), false);
                    
                    // 通过BeanDefinitionBuilder创建bean定义
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(SimpleMessageListenerContainer.class);
                    beanDefinitionBuilder.addConstructorArgValue(connectionFactory);
                    beanDefinitionBuilder.addPropertyValue("messageListener", new RabbitMqListener(v.getBean(), v.getMethod(), v.getTopic(), rabbitMqTemplate));
                    beanDefinitionBuilder.addPropertyValue("queues", new Queue(v.getTopic()));
                    beanDefinitionBuilder.addPropertyValue("concurrentConsumers", v.getConcurrentConsumers());//设置当前的消费者数量
                    beanDefinitionBuilder.addPropertyValue("acknowledgeMode", AcknowledgeMode.MANUAL);//设置手动签收
//                    beanDefinitionBuilder.addPropertyValue("transactionManager", new RabbitTransactionManager(connectionFactory));//设置事务
                    // 注册bean
                    defaultListableBeanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getRawBeanDefinition());
                }
            }
            i++;
        }
        return null;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RabbitPropertiesBeanPostProcessor rabbitPropertiesBeanPostProcessor() {
        return new RabbitPropertiesBeanPostProcessor();
    }
    
}
