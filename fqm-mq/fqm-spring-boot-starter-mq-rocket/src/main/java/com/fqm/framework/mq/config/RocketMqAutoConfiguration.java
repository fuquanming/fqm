package com.fqm.framework.mq.config;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.apache.rocketmq.spring.support.RocketMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
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
import org.springframework.messaging.converter.StringMessageConverter;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.RocketMqListener;
import com.fqm.framework.mq.template.RocketMqTemplate;
import com.google.common.base.Preconditions;

/**
 * Rocket消息队列自动装配
 * @version 
 * @author 傅泉明
 */
@Configuration
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class RocketMqAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    } 
    
    @Value("${rocketmq.name-server:}")
    private String nameServer;
    
    /**
     * 查看RocketMQAutoConfiguration.defaultMQProducer(RocketMQProperties rocketMQProperties)
     * 先初始化DefaultMQProducer,解决没有配置rocketmq.producer.group，就没有实例化DefaultMQProducer
     * @param rocketMqProperties
     * @return
     */
    @Bean("defaultMQProducer")
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    public DefaultMQProducer defaultMqProducer(RocketMQProperties rocketMqProperties) {
        RocketMQProperties.Producer producerConfig = rocketMqProperties.getProducer();
        String nameServerProperties = rocketMqProperties.getNameServer();
        if (producerConfig == null) {
            producerConfig = new RocketMQProperties.Producer();
        }
        String groupName = "fqmGroup";

        String accessChannel = rocketMqProperties.getAccessChannel();

        String ak = producerConfig.getAccessKey();
        String sk = producerConfig.getSecretKey();
        boolean isEnableMsgTrace = producerConfig.isEnableMsgTrace();
        String customizedTraceTopic = producerConfig.getCustomizedTraceTopic();

        DefaultMQProducer producer = RocketMQUtil.createDefaultMQProducer(groupName, ak, sk, isEnableMsgTrace, customizedTraceTopic);

        producer.setNamesrvAddr(nameServerProperties);
        if (accessChannel != null && !accessChannel.equals("")) {
            producer.setAccessChannel(AccessChannel.valueOf(accessChannel));
        }
        producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
        producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        producer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
        producer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
        producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());
        
        return producer;
    }
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public RocketMqTemplate rocketMqTemplate(MqFactory mqFactory, RocketMQTemplate template) {
        RocketMqTemplate rocketMqTemplate = new RocketMqTemplate(template);
        mqFactory.addMqTemplate(rocketMqTemplate);
        return rocketMqTemplate;
    }
    
    @Override
    public void afterSingletonsInstantiated() {
        MqListenerAnnotationBeanPostProcessor mq = applicationContext.getBean(MqListenerAnnotationBeanPostProcessor.class);
        MqProperties mp = applicationContext.getBean(MqProperties.class);
        
        int i = 0;
        for (MqListenerParam v : mq.getListeners()) {
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties == null) {
                properties = getProperties(mp, name, properties);
            }
            if (properties != null && MqMode.ROCKET.name().equalsIgnoreCase(properties.getBinder())) {
                String group = properties.getGroup();
                String topic = properties.getTopic();
                Preconditions.checkArgument(StringUtils.isNotBlank(group), "Please specific [group] under mq configuration.");
                Preconditions.checkArgument(StringUtils.isNotBlank(topic), "Please specific [topic] under mq configuration.");
                String beanName = "rocketListener." + i;
                // 动态注册
                //将applicationContext转换为ConfigurableApplicationContext
                ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
                // 获取bean工厂并转换为DefaultListableBeanFactory
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
                if (!applicationContext.containsBean(beanName)) {
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultRocketMQListenerContainer.class);
                    beanDefinitionBuilder.addPropertyValue("consumerGroup", group);
                    beanDefinitionBuilder.addPropertyValue("nameServer", nameServer);
                    beanDefinitionBuilder.addPropertyValue("topic", topic);
                    
                    beanDefinitionBuilder.addPropertyValue("messageConverter", new StringMessageConverter(StandardCharsets.UTF_8));
                    
                    beanDefinitionBuilder.addPropertyValue("rocketMQMessageListener", rocketMqMessageListener(nameServer, topic, group, 1));
                    beanDefinitionBuilder.addPropertyValue("rocketMQListener", new RocketMqListener(v.getBean(), v.getMethod(), topic, applicationContext.getBean(RocketMqTemplate.class)));
                    // 注册bean
                    AbstractBeanDefinition bean = beanDefinitionBuilder.getRawBeanDefinition();
                    
                    defaultListableBeanFactory.registerBeanDefinition(beanName, bean);
                    
                    try {
                        DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) applicationContext.getBean(beanName);
                        // 修改私有属性，数据传输类型，为string
                        FieldUtils.writeDeclaredField(container, "messageType", String.class, true);
                        // 指定从第一条消息开始消费
                        container.getConsumer().setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                        container.getConsumer().setInstanceName(beanName);
                        // 消费失败1次，立即入死信队列 topic="%DLQ%" + v.getGroup()
                        // 第一次10s，第二次30s，第三次60s
                        container.getConsumer().setMaxReconsumeTimes(1);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    i++;
                    logger.info("Init RocketMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
                }
            }
        }
    }

    private MqConfigurationProperties getProperties(MqProperties mp, String name, MqConfigurationProperties properties) {
        // 遍历mp.mqs
        for (MqConfigurationProperties mcp : mp.getMqs().values()) {
            if (mcp.getName().equals(name) && MqMode.ROCKET.name().equalsIgnoreCase(mcp.getBinder())) {
                properties = mcp;
                break;
            }
        }
        return properties;
    }

    private RocketMQMessageListener rocketMqMessageListener(String nameServer, String topic, String group, int concurrentConsumers) {
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
