package com.fqm.framework.common.mq.template;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fqm.framework.common.core.util.ReturnParamUtil;
import com.fqm.framework.common.mq.callback.RabbitListenableFutureCallback;
import com.fqm.framework.common.mq.client.producer.SendCallback;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.system.SystemUtil;

/**
 * Rabbit消息队列，发送都是异步消息
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RabbitTemplate rabbitTemplate;
    private AmqpAdmin amqpAdmin;
    /** 队列列表 */
    private Set<String> topicSet = new ConcurrentHashSet<>();
    /** 延迟任务交换机列表 */
    private Set<String> exchangeSet = new ConcurrentHashSet<>();
    
    private AtomicLong atomicLong = new AtomicLong();
    
    private String hostAddress = SystemUtil.getHostInfo().getAddress();
    
    private long pid = SystemUtil.getCurrentPID();
    /** 延迟任务的信息头 */
    public static final String HEADER_DELAY = "rabbit-delay";
    
    public RabbitMqTemplate(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }
    
    public Set<String> getExchangeSet() {
        return exchangeSet;
    }
    
    public void initTopic(String topic, boolean isDelay) {
        // 延迟任务
        if (isDelay) {
            if (!exchangeSet.contains(topic)) {
                // String name:名称, boolean durable:是否持久化, boolean exclusive:是否排他（只能一个人连接）, boolean autoDelete:是否自动删除
                Queue queue = new Queue(topic, true, false, false);
                String queueResult = amqpAdmin.declareQueue(queue);
                logger.info("queueInit=" + queueResult);
                topicSet.add(topic);
                
                TopicExchange customExchange = new TopicExchange(topic, true, false);
                customExchange.setDelayed(true);// 延迟队列
                amqpAdmin.declareExchange(customExchange);
                amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(customExchange).with(topic));
                exchangeSet.add(topic);
                logger.info("exchangeInit=" + queueResult);
            }
        } else {
            if (!topicSet.contains(topic)) {
                // String name:名称, boolean durable:是否持久化, boolean exclusive:是否排他（只能一个人连接）, boolean autoDelete:是否自动删除
                Queue queue = new Queue(topic, true, false, false);
                String queueResult = amqpAdmin.declareQueue(queue);
                logger.info("queueInit=" + queueResult);
                topicSet.add(topic);
            }
        }
        
    }
    /**
     * 消息ID
     * @return
     */
    private String getId() {
        return String.format("%s@%d@%s", hostAddress, pid, atomicLong.incrementAndGet());
    }
    
    private ImmutablePair<CorrelationData, RabbitListenableFutureCallback> getCorrelationData() {
        String id = getId();
        CorrelationData correlationData = new CorrelationData(id);
        RabbitListenableFutureCallback callback = new RabbitListenableFutureCallback(Thread.currentThread(), id);
        correlationData.getFuture().addCallback(callback);
        return ReturnParamUtil.of(correlationData, callback);
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        try {
            initTopic(topic, false);
            
            ImmutablePair<CorrelationData, RabbitListenableFutureCallback> params = getCorrelationData();
            CorrelationData correlationData = params.left;
            RabbitListenableFutureCallback callback = params.right;
            
            rabbitTemplate.convertAndSend("", topic, str, correlationData);
            
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));// 最多等3秒
            if (callback.isError()) {
                logger.info("RabbitMqProducer.asyncSend.error->topic=[{}],message=[{}]", topic, str);
                return false;
            } else {
                logger.info("RabbitMqProducer.asyncSend.success->topic=[{}],message=[{}]", topic, str);
                return true;
            }
        } catch (Exception e) {
            logger.error("RabbitMqProducer.asyncSend.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 需要安装x-delayed-message延迟插件
     * @see com.fqm.framework.common.mq.template.MqTemplate#syncDelaySend(java.lang.String, java.lang.Object, java.lang.Integer)
     *
     */
    @Override
    public boolean syncDelaySend(String topic, Object msg, Integer delayTime) {
        String str = getJsonStr(msg);
        try {
            initTopic(topic, true);
            
            ImmutablePair<CorrelationData, RabbitListenableFutureCallback> params = getCorrelationData();
            CorrelationData correlationData = params.left;
            RabbitListenableFutureCallback callback = params.right;
            
            rabbitTemplate.convertAndSend(topic, topic, str, message ->{
                message.getMessageProperties().setDelay(delayTime);
                // 标识消息是延迟任务，RabbitReturnsCallback判断如果是延迟任务则不认为是异常
                message.getMessageProperties().getHeaders().put(HEADER_DELAY, delayTime);
                return message;
            }, correlationData);
            
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));// 最多等3秒
            if (callback.isError()) {
                logger.info("RabbitMqProducer.syncDelaySend.error->topic=[{}],message=[{}],delayTime=[{}]", topic, str, delayTime);
                return false;
            } else {
                logger.info("RabbitMqProducer.syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}]", topic, str, delayTime);
                return true;
            }
        } catch (Exception e) {
            logger.error("RabbitMqProducer.syncDelaySend.error->topic=[" + topic + "],message=[" + str + "],delayTime=[" + delayTime + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        try {
            initTopic(topic, false);
            
            ImmutablePair<CorrelationData, RabbitListenableFutureCallback> params = getCorrelationData();
            CorrelationData correlationData = params.left;
            
            rabbitTemplate.convertAndSend("", topic, str, correlationData);
            
            logger.info("RabbitMqProducer.asyncSend->topic=[{}],message=[{}]", topic, str);
        } catch (Exception e) {
            logger.error("RabbitMqProducer.asyncSend.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
    }
    
}