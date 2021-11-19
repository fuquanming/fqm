package com.fqm.framework.common.mq.config;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.mq.MqFactory;
import com.fqm.framework.common.mq.MqMode;
import com.fqm.framework.common.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.common.mq.listener.MqListenerParam;
import com.fqm.framework.common.mq.listener.RabbitMqListener;
import com.fqm.framework.common.mq.template.RabbitMqTemplate;

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
    @Order(200)
    public RabbitMqTemplate rabbitMqTemplate(MqFactory mqFactory, 
            RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        RabbitMqTemplate rabbitMqTemplate = new RabbitMqTemplate(rabbitTemplate, amqpAdmin);
        mqFactory.addMqTemplate(rabbitMqTemplate);
        return rabbitMqTemplate;
    }
    
    @Resource
    MqListenerAnnotationBeanPostProcessor mq;
    @Resource
    ApplicationContext applicationContext;
    @Resource
    ConnectionFactory connectionFactory;
    @Resource
    RabbitMqTemplate rabbitMqTemplate;
    @Resource
    RabbitTemplate rabbitTemplate;
    
    @PostConstruct
    public void init() {
        /** 设置确认回调 */
        rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
            /**
             * @param ccorrelationData  消息唯一关联数据（失败时，有该对象）
             * @param ack               消息是否发生成功
             * @param cause             失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("correlationData=[" + correlationData + "],ack=[" + ack + "],cause=[" + cause + "]");
            }
        });
                
        /** 消息抵达队列失败回调 */
        rabbitTemplate.setReturnsCallback(new ReturnsCallback() {
            /**
             * ReturnedMessage:
             * message：投递失败的消息;
             * replyCode：回复的状态码;
             * replyText：回复的文本内容;
             * exchange：当时这个消息发给那个交换机;
             * routingKey：当时这个消息用哪个路由键
             */
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                System.out.println("Fail Message[" + returned.getMessage() + "],replyCode=[" + returned.getReplyCode() + "],replyText=[" + returned.getReplyText() + "],exchange=[" + returned.getExchange() + "],routingKey=[" + returned.getRoutingKey() + "]");
            }
        });
        
        
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
                    rabbitMqTemplate.initTopic(v.getDestination());
                    
                    // 通过BeanDefinitionBuilder创建bean定义
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(SimpleMessageListenerContainer.class);
                    beanDefinitionBuilder.addConstructorArgValue(connectionFactory);
                    beanDefinitionBuilder.addPropertyValue("messageListener", new RabbitMqListener(v.getBean(), v.getMethod(), v.getDestination(), rabbitMqTemplate));
                    beanDefinitionBuilder.addPropertyValue("queues", new Queue(v.getDestination()));
                    beanDefinitionBuilder.addPropertyValue("concurrentConsumers", v.getConcurrentConsumers());//设置当前的消费者数量
                    beanDefinitionBuilder.addPropertyValue("acknowledgeMode", AcknowledgeMode.MANUAL);//设置手动签收
//                    beanDefinitionBuilder.addPropertyValue("transactionManager", new RabbitTransactionManager(connectionFactory));//设置事务
                    // 注册bean
                    defaultListableBeanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getRawBeanDefinition());
                    
                }
            }
            i++;
        }
    }

}
