package com.fqm.framework.mq.config;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.RocketMqListener;
import com.fqm.framework.mq.template.RocketMqTemplate;

/**
 * Rocket????????????????????????
 * 
 * @version 
 * @author ?????????
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
     * ?????? RocketMQAutoConfiguration.defaultMQProducer(RocketMQProperties rocketMQProperties)
     * ????????????DefaultMQProducer,??????????????????rocketmq.producer.group?????????????????????DefaultMQProducer
     * @param rocketMqProperties
     * @return
     */
    @Bean("defaultMQProducer")
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    DefaultMQProducer defaultMqProducer(RocketMQProperties rocketMqProperties) {
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
        if (accessChannel != null && !"".equals(accessChannel)) {
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
    RocketMqTemplate rocketMqTemplate(MqFactory mqFactory, RocketMQTemplate template) {
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
            if (properties != null && MqMode.ROCKET.equalMode(properties.getBinder())) {
                String group = properties.getGroup();
                String topic = properties.getTopic();
                Assert.isTrue(StringUtils.hasText(topic), "Please specific [topic] under mq.mqs." + name + " configuration.");
                Assert.isTrue(StringUtils.hasText(group), "Please specific [group] under mq.mqs." + name + " configuration.");
                String beanName = "rocketListener." + i;
                // ????????????
                //???applicationContext?????????ConfigurableApplicationContext
                ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
                // ??????bean??????????????????DefaultListableBeanFactory
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
                if (!applicationContext.containsBean(beanName)) {
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultRocketMQListenerContainer.class);
                    beanDefinitionBuilder.addPropertyValue("consumerGroup", group);
                    beanDefinitionBuilder.addPropertyValue("nameServer", nameServer);
                    beanDefinitionBuilder.addPropertyValue("topic", topic);
                    
                    beanDefinitionBuilder.addPropertyValue("messageConverter", new StringMessageConverter(StandardCharsets.UTF_8));
                    
                    beanDefinitionBuilder.addPropertyValue("rocketMQMessageListener", rocketMqMessageListener(nameServer, topic, group, 1));
                    beanDefinitionBuilder.addPropertyValue("rocketMQListener", new RocketMqListener(v.getBean(), v.getMethod(), topic, applicationContext.getBean(RocketMqTemplate.class)));
                    // ??????bean
                    AbstractBeanDefinition bean = beanDefinitionBuilder.getRawBeanDefinition();
                    
                    defaultListableBeanFactory.registerBeanDefinition(beanName, bean);
                    
                    try {
                        DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) applicationContext.getBean(beanName);
                        // ?????????????????????????????????????????????string
                        FieldUtils.writeDeclaredField(container, "messageType", String.class, true);
                        // ????????????????????????????????????
                        container.getConsumer().setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                        container.getConsumer().setInstanceName(beanName);
                        // ????????????1??????????????????????????? topic="%DLQ%" + v.getGroup()
                        // ?????????10s????????????30s????????????60s
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
                return MessageModel.CLUSTERING;// ??????
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
