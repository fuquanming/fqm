package com.fqm.framework.mq.template;

import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.util.RocketDelayUtil;

/**
 * Rocket消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public class RocketMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RocketMQTemplate template;
    
    private String messageStr = "],message=[";
    
    public RocketMqTemplate(RocketMQTemplate rocketMqTemplate) {
        this.template = rocketMqTemplate;
    }
    
    @Override
    public MqMode getMqMode() {
        return MqMode.ROCKET;
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        try {
            SendResult sendResult = template.syncSend(topic, MessageBuilder.withPayload(str).build());
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                logger.info("RocketMqProducer.syncSend.success->topic=[{}],message=[{}],offset=[{}]", topic, str, "");
                return true;
            } else {
                logger.error("RocketMqProducer.syncSend.error->topic=[{}],message=[{}],msg=[{}]", topic, str, sendResult);
            }
        } catch (Exception e) {
            logger.error("RocketMqProducer.syncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        try {
            template.asyncSend(topic, MessageBuilder.withPayload(str).build(), new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    logger.info("RocketMqProducer.asyncSend.success->topic=[{}],message=[{}],offset=[{}]", topic, str, "");
                    sendCallback.onSuccess(null);
                }
                
                @Override
                public void onException(Throwable e) {
                    logger.error("RocketMqProducer.asyncSend.error->topic=[" + topic + messageStr + str + "]", e);
                    sendCallback.onException(e);
                }
            });
        } catch (Exception e) {
            logger.error("RocketMqProducer.asyncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
    }

    @Override
    public boolean syncDelaySend(String topic, Object msg, int delayTime, TimeUnit timeUnit) {
        String str = RocketDelayUtil.buildDelayMsg(msg, delayTime, timeUnit);
        try {
            Message<String> message = MessageBuilder.withPayload(str)
//                    .setHeader("delayLevel", 1)
                    .build();
            // 一、延迟时间存在十八个等级 (1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h )
            // 通过18级的时间，每次比较最近所在的级别时间投递进去。
            SendResult sendResult = template.syncSend(topic, message, 5000, RocketDelayUtil.getDelayLevel(delayTime, timeUnit));
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                logger.info("RocketMqProducer.syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
                return true;
            } else {
                logger.error("RocketMqProducer.syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
            }
            
            // 二、设置DefaultRocketMQListenerContainer.DefaultMQPushConsumer.DefaultMQPushConsumerImpl.isPause(true) 暂停消费
            // PullMessageService.run 获取拉取消息请求
            // 如果是同一个topic下，会影响原正常的消息接收
            // 1、延迟消息就投递到其他topic下，发送topic通知客户端及发送延迟的topic消息（多级别1s，2s等）
            // 2、客户端监听通知topic，获取延迟的topic及时间，开启时间轮（宕机，丢失延迟的topic及时间，消费通知topic后直接进入死信队列，监听死信队列判断是否开启时间轮），定时开启消费延迟的topic的消费，暂停消费会有延迟
            // 3、客户端获取到延迟的topic消息，投递到正常监听topic下。
            // 4、客户端获取到延迟消息
        } catch (Exception e) {
            logger.error("RocketMqProducer.syncDelaySend.error->topic=[" + topic + messageStr + str + "],delayTime=[" + delayTime + "],timeUnit=[" + timeUnit + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
}
