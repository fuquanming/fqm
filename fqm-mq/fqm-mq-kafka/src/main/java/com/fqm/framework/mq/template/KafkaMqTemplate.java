package com.fqm.framework.mq.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.client.producer.SendCallback;

/**
 * Kafka消息队列
 * 
 * @version 
 * @author 傅泉明
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private String messageStr = "],message=[";
    
    private KafkaTemplate kafkaTemplate;
    
    public KafkaMqTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    public MqMode getMqMode() {
        return MqMode.KAFKA;
    }
    
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        final boolean[] flag = {true};
        try {
            ListenableFuture future = kafkaTemplate.send(topic, str);
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(Throwable e) {
                    logger.error("KafkaMqProducer.syncSend.error->topic=[" + topic + messageStr + str + "]", e);
                    e.printStackTrace();
                    flag[0] = false;
                }
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    logger.info("KafkaMqProducer.syncSend.success->topic=[{}],message=[{}],offset=[{}]", topic, str, result.getRecordMetadata().offset());
                }
            });
            future.get();
            return flag[0];
        }catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("KafkaMqProducer.syncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        try {
            ListenableFuture future = kafkaTemplate.send(topic, str);
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(Throwable e) {
                    logger.error("KafkaMqProducer.asyncSend.error->topic=[" + topic + messageStr + str + "]", e);
                    sendCallback.onException(e);
                    e.printStackTrace();
                }
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    logger.info("KafkaMqProducer.asyncSend.success->topic=[{}],message=[{}],offset=[{}]", topic, str, result.getRecordMetadata().offset());
                    sendCallback.onSuccess(null);
                }
            });
        } catch (Exception e) {
            logger.error("KafkaMqProducer.asyncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
    }

}
