package com.fqm.test.mq.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;
import com.fqm.framework.mq.util.MqProducer;
import com.fqm.test.controller.BaseController;
import com.fqm.test.model.User;

@RestController
public class KafkaMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    
    public static final String BUSINESS_CREATE_ORDER = "a";
    public static final String BUSINESS_CREATE_ORDER_1 = "a1";
    public static final String BUSINESS_CREATE_ORDER_DEAD = "a-dead";
    public static final String BUSINESS_CREATE_ORDER_DEAD_1 = "a1-dead";
    @Resource
    MqProducer mqProducer;

    @MqListener(name = BUSINESS_CREATE_ORDER)
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---kafka---1={}", message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }
    
    @MqListener(name = BUSINESS_CREATE_ORDER_1)
    public void receiveMessage2(User message) {
        logger.info("receiveMessage---kafka---2={}", message.getName());
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = BUSINESS_CREATE_ORDER_DEAD)
    public void mqDLQ(String message) {
        logger.info("kafka.DLQ={}", message);
    }
    @MqListener(name = BUSINESS_CREATE_ORDER_DEAD_1)
    public void mqDLQ1(String message) {
        logger.info("kafka1.DLQ={}", message);
    }
    
    @GetMapping("/mq/kafka/sendMessage")
    public Object sendKafkaMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_CREATE_ORDER).syncSend(user);
            logger.info("kafka.send->{}", flag);

            mqProducer.getProducer(BUSINESS_CREATE_ORDER_1).asyncSend(user, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("onSuccess");
                }
                @Override
                public void onException(Throwable e) {
                    System.out.println("onException");
                }
            });
            // 通过消息模板发送消息
//            mqFactory.getMqTemplate(mqProducer.getBinder(BUSINESS_CREATE_ORDER_1))
//                .syncSend(mqProducer.getTopic(BUSINESS_CREATE_ORDER_1), user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
