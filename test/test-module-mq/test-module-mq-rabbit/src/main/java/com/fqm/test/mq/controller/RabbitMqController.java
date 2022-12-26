package com.fqm.test.mq.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.common.core.util.RandomUtil;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;
import com.fqm.framework.mq.util.MqProducer;
import com.fqm.test.controller.BaseController;
import com.fqm.test.model.User;

@RestController
public class RabbitMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    
    public static final String BUSINESS_CREATE_ORDER = "b";
    public static final String BUSINESS_CREATE_ORDER_1 = "b1";
    public static final String BUSINESS_CREATE_ORDER_DEAD = "b-dead";
    public static final String BUSINESS_CREATE_ORDER_DEAD_1 = "b1-dead";
    @Resource
    MqProducer mqProducer;

    @MqListener(name = BUSINESS_CREATE_ORDER)
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---rabbit---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = BUSINESS_CREATE_ORDER_1)
    public void receiveMessage2(User message) {
        logger.info("receiveMessage---rabbit---2=" + message.getName());
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = BUSINESS_CREATE_ORDER_DEAD)
    public void mqDLQ(String message) {
        logger.info("rabbit.DLQ=" + message);
    }
    
    @MqListener(name = BUSINESS_CREATE_ORDER_DEAD_1)
    public void mqDLQ1(String message) {
        logger.info("rabbit1.DLQ=" + message);
    }
    
    @GetMapping("/mq/rabbit/sendMessage")
    public Object sendRabbitMessage() {
        User user = getUser();
        user.setName("张三" + RandomUtil.nextInt());
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_CREATE_ORDER).syncSend(user);
            logger.info("rabbit.send->{}", flag);

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

    @GetMapping("/mq/rabbit/sendDelayMessage")
    public Object sendRabbitDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_CREATE_ORDER).syncDelaySend(user, 3, TimeUnit.SECONDS);
            logger.info("rabbit.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
