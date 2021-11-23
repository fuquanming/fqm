package com.fqm.framework.common.mq.config;

import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.fqm.framework.common.mq.callback.RabbitListenableFutureCallback;
import com.fqm.framework.common.mq.template.RabbitMqTemplate;

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

    private Logger logger = LoggerFactory.getLogger(getClass());
    
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
        } else if (targetClass == RabbitTemplate.class) {/** 设置rabbitTemplate的回调函数 */
            RabbitTemplate rabbitTemplate = (RabbitTemplate) bean;
            /** 设置确认回调，可以用发送添加CcorrelationData对象，
             * 使用 correlationData.getFuture().addCallback 替代 */
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
                    logger.info(Thread.currentThread().getId() + ",Fail Message[" + returned.getMessage() + "],replyCode=[" + returned.getReplyCode() + "],replyText=[" + returned.getReplyText() + "],exchange=[" + returned.getExchange() + "],routingKey=[" + returned.getRoutingKey() + "]");
                    
                    RabbitMqTemplate rabbitMqTemplate = beanFactory.getBean(RabbitMqTemplate.class);
                    String id = returned.getMessage().getMessageProperties().getHeader("spring_returned_message_correlation");
                    if (id != null) {
                        Map<String, RabbitListenableFutureCallback> callbackMap = rabbitMqTemplate.getFutureCallbackMap();
                        RabbitListenableFutureCallback futureCallback = callbackMap.get(id);
                        if (futureCallback != null) {
                            if (futureCallback.getSendCallback() != null) {// 异步消息
                                futureCallback.setMsgError(true);
                                LockSupport.unpark(futureCallback.getCallbackThread());
                                futureCallback.getSendCallback().onException(new RuntimeException(returned.getReplyText()));
                            } else {// 同步消息
                                futureCallback.setMsgError(true);
                                LockSupport.unpark(futureCallback.getCallbackThread());
                            }
                        }
                    }
                }
            });
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
