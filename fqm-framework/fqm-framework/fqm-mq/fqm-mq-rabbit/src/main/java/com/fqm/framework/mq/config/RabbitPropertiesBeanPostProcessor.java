package com.fqm.framework.mq.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.fqm.framework.mq.callback.RabbitReturnsCallback;
import com.fqm.framework.mq.template.RabbitMqTemplate;

/**
 * 拦截 RabbitProperties，修改配置信息，接口PriorityOrdered优先级
 * 发送，确认回调，队列失败回调都是不同的线程
 * 1.设置确认回调
 * 2.消息抵达队列失败回调
 * 3.设置RabbitTemplate的回调函数ConfirmCallback，ReturnsCallback
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitPropertiesBeanPostProcessor implements BeanPostProcessor, PriorityOrdered, BeanFactoryAware {

    private BeanFactory beanFactory;
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass == RabbitProperties.class) {
            RabbitProperties properties = (RabbitProperties) bean;
            /** 1.设置确认回调 */
            properties.setPublisherConfirmType(ConfirmType.CORRELATED);
            /** 2.消息抵达队列失败回调 */
            properties.setPublisherReturns(true);
            properties.getTemplate().setMandatory(true);//开启强制委托模式
        } else if (targetClass == RabbitMqTemplate.class) {/** 设置rabbitTemplate的回调函数 */
            /** RabbitMqTemplate 实例化前RabbitTemplate会先实例化， */
            RabbitMqTemplate rabbitMqTemplate = (RabbitMqTemplate) bean;
            /** 设置确认回调，多线程回调。可以用发送添加CcorrelationData对象，
             * 使用 correlationData.getFuture().addCallback 替代
             * addCallback 优于该ConfirmCallback前回调
             *  */
//            rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
//                /**
//                 * @param ccorrelationData  消息唯一关联数据（发送时带该对象，则返回时有该对象）
//                 * @param ack               消息是否发生成功
//                 * @param cause             失败原因
//                 */
//                @Override
//                public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                    System.out.println(Thread.currentThread().getId() + ",correlationData=[" + correlationData + "],ack=[" + ack + "],cause=[" + cause + "]");
//                }
//            });
                    
            /** 消息抵达队列失败回调，多线程回调 */
            RabbitTemplate rabbitTemplate = beanFactory.getBean(RabbitTemplate.class);
            rabbitTemplate.setReturnsCallback(new RabbitReturnsCallback(rabbitMqTemplate));
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
