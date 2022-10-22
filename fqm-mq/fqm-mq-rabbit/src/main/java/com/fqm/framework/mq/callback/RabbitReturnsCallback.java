package com.fqm.framework.mq.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnsCallback;

import com.fqm.framework.mq.template.RabbitMqTemplate;

/**
 * 消息抵达队列失败回调，多线程回调
 * 延迟队列插件（x-delayed-message）会一直触发回调，因为该消息是延迟投递到队列中，没有及时投递到队列中就会触发该回调
 * 在回调时判断如果是延迟队列的信息(头信息判断)交换机就不认为是异常
 * 
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitReturnsCallback implements ReturnsCallback {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
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
        // 如果是延迟队列信息，会一直回调该方法，[(Body:'{"id":null,"createTime":null,"updateTime":null,"name":"张三","age":2,"depts":null}' MessageProperties [headers={spring_returned_message_correlation=192.168.1.191@32828@2}, contentType=text/plain, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, receivedDelay=3000, deliveryTag=0])],replyCode=[312],replyText=[NO_ROUTE],exchange=[my-topic],routingKey=[my-topic]
        // 判断头信息是否有延迟标识
        Object headerDelay = returned.getMessage().getMessageProperties().getHeader(RabbitMqTemplate.HEADER_DELAY);
        if (headerDelay != null) {
            return;
        }
        logger.info("RabbitMqProducer.error->Fail Message[{}],replyCode=[{}],replyText=[{}],exchange=[{}],routingKey=[{}]", 
                returned.getMessage(), returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
    }
}
