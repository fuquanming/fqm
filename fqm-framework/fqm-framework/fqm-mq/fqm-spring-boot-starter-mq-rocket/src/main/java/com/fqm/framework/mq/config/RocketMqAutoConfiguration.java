package com.fqm.framework.mq.config;

import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.converter.StringMessageConverter;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.RocketMqListener;
import com.fqm.framework.mq.template.RocketMqTemplate;

/**
 * Rocket消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class RocketMqAutoConfiguration {
    
    @Value("${rocketmq.name-server:}")
    private String nameServer;
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public RocketMqTemplate rocketMqTemplate(MqFactory mqFactory, RocketMQTemplate rocketMQTemplate) {
        RocketMqTemplate rocketMqTemplate = new RocketMqTemplate(rocketMQTemplate);
        mqFactory.addMqTemplate(rocketMqTemplate);
        return rocketMqTemplate;
    }
    
    @Resource
    MqListenerAnnotationBeanPostProcessor mq;
    @Resource
    ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        int i = 0;
        for (MqListenerParam v : mq.getListeners()) {
            String name = "rocketListener." + i;
            // 动态注册
            //将applicationContext转换为ConfigurableApplicationContext
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            // 获取bean工厂并转换为DefaultListableBeanFactory
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
            if (applicationContext.containsBean(name) == false) {

                if (MqMode.rocket.name().equals(v.getBinder())) {
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultRocketMQListenerContainer.class);
                    beanDefinitionBuilder.addPropertyValue("consumerGroup", v.getGroup());
                    beanDefinitionBuilder.addPropertyValue("nameServer", nameServer);
                    beanDefinitionBuilder.addPropertyValue("topic", v.getTopic());
                    
                    beanDefinitionBuilder.addPropertyValue("messageConverter", new StringMessageConverter(Charset.forName("UTF-8")));
                    
                    beanDefinitionBuilder.addPropertyValue("rocketMQMessageListener", 
                            rocketMQMessageListener(nameServer, v.getTopic(), v.getGroup(), v.getConcurrentConsumers()));
                    beanDefinitionBuilder.addPropertyValue("rocketMQListener", new RocketMqListener(v.getBean(), v.getMethod()));
                    // 注册bean
                    AbstractBeanDefinition bean = beanDefinitionBuilder.getRawBeanDefinition();
                    
                    defaultListableBeanFactory.registerBeanDefinition(name, bean);
                    
                    try {
                        DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) applicationContext.getBean(name);
                        FieldUtils.writeDeclaredField(container, "messageType", String.class, true);// 修改私有属性，数据传输类型，为string
                        container.getConsumer().setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);// 指定从第一条消息开始消费
                        container.getConsumer().setInstanceName(name);
                        // 消费失败1次，立即入死信队列 topic="%DLQ%" + v.getGroup()
                        // 第一次10s，第二次30s，第三次60s
                        container.getConsumer().setMaxReconsumeTimes(1);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            i++;
        }
    }

    private RocketMQMessageListener rocketMQMessageListener(String nameServer, String topic, String group, int concurrentConsumers) {
        return new RocketMQMessageListener() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
            
            @Override
            public String topic() {
                return topic;
            }
            
            @Override
            public SelectorType selectorType() {
                return SelectorType.TAG;
            }
            
            @Override
            public String selectorExpression() {
                return null;
            }
            
            @Override
            public String secretKey() {
                return null;
            }
            
            @Override
            public int replyTimeout() {
                return 3000;
            }
            
            @Override
            public String nameServer() {
                return nameServer;
            }
            
            @Override
            public MessageModel messageModel() {
                return MessageModel.CLUSTERING;// 集群
            }
            
            @Override
            public int maxReconsumeTimes() {
                return 0;
            }
            
            @Override
            public boolean enableMsgTrace() {
                return false;
            }
            
            @Override
            public String customizedTraceTopic() {
                return topic;
            }
            
            @Override
            public String consumerGroup() {
                return group;
            }
            
            @Override
            public long consumeTimeout() {
                return 15L;
            }
            
            @Override
            public int consumeThreadMax() {
                return concurrentConsumers;
            }
            
            @Override
            public ConsumeMode consumeMode() {
                return ConsumeMode.CONCURRENTLY;
            }
            
            @Override
            public String accessKey() {
                return null;
            }
            
            @Override
            public String accessChannel() {
                return null;
            }
        };
    }
}
