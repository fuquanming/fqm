package com.fqm.framework.common.mq.template;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;

import com.fqm.framework.common.mq.client.producer.SendCallback;

/**
 * Rocket消息队列
 * 
 * @version 
 * @author 傅泉明
 */
public class RocketMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RocketMQTemplate rocketMqTemplate;
    
    public RocketMqTemplate(RocketMQTemplate rocketMqTemplate) {
        this.rocketMqTemplate = rocketMqTemplate;
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        try {
            SendResult sendResult = rocketMqTemplate.syncSend(topic, MessageBuilder.withPayload(str).build());
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                logger.info("RocketMqProducer.success->topic=[{}],message=[{}],offset=[{}]", topic, str, "");
                return true;
            } else {
                logger.error("RocketMqProducer.error->topic=[{}],message=[{}],msg=[{}]", topic, str, sendResult);
            }
        } catch (Exception e) {
            logger.error("RocketMqProducer.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        try {
            rocketMqTemplate.asyncSend(topic, MessageBuilder.withPayload(str).build(), new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    logger.info("RocketMqProducer.success->topic=[{}],message=[{}],offset=[{}]", topic, str, "");
                    sendCallback.onSuccess(null);
                }
                
                @Override
                public void onException(Throwable e) {
                    logger.error("RocketMqProducer.error->topic=[" + topic + "],message=[" + str + "]", e);
                    sendCallback.onException(e);
                }
            });
        } catch (Exception e) {
            logger.error("RocketMqProducer.error->topic=[" + topic + "],message=[" + str + "]", e);
            e.printStackTrace();
        }
    }


}
