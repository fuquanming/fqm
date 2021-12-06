package com.fqm.framework.common.mq.callback;

import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;

import com.fqm.framework.common.mq.template.RabbitMqTemplate;

/**
 * 消息抵达队列失败回调，多线程回调
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitReturnsCallback implements ReturnsCallback {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private RabbitMqTemplate rabbitMqTemplate;
    
    public RabbitReturnsCallback(RabbitMqTemplate rabbitMqTemplate) {
        this.rabbitMqTemplate = rabbitMqTemplate;
    }
    
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
        logger.info("RabbitMqProducer.error->Fail Message[" + returned.getMessage() + "],replyCode=[" + returned.getReplyCode() + "],replyText=[" + returned.getReplyText() + "],exchange=[" + returned.getExchange() + "],routingKey=[" + returned.getRoutingKey() + "]");
//        String id = returned.getMessage().getMessageProperties().getHeader("spring_returned_message_correlation");
//        if (id != null) {
//            Map<String, RabbitListenableFutureCallback> callbackMap = rabbitMqTemplate.getFutureCallbackMap();
//            System.out.println(callbackMap.size());
//            RabbitListenableFutureCallback futureCallback = callbackMap.get(id);
//            if (futureCallback != null) {
//                if (futureCallback.getSendCallback() != null) {// 异步消息
//                    futureCallback.setError(true);
//                    LockSupport.unpark(futureCallback.getCallbackThread());
//                    futureCallback.getSendCallback().onException(new RuntimeException(returned.getReplyText()));
//                } else {// 同步消息
//                    futureCallback.setError(true);
//                    futureCallback.setErrorMsg(returned.getReplyText());
//                    LockSupport.unpark(futureCallback.getCallbackThread());
//                }
//                callbackMap.remove(id);
//            }
//        }
    }

}
