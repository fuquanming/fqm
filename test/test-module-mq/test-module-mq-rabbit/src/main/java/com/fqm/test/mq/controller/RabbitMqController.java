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
    /** 消息主题名称：对应配置文件 mq.mqs.xxx */
    public static final String TOPIC = "rabbit-topic";
    public static final String TOPIC_1 = "rabbit-topic1";
    /** 死信主题名称：对应配置文件 mq.mqs.xxx，死信主题：topic + ".DLQ" */
    public static final String TOPIC_DEAD = "rabbit-topic.DLQ";
    public static final String TOPIC_1_DEAD = "rabbit-topic1.DLQ";
    @Resource
    MqProducer mqProducer;

    @MqListener(name = TOPIC)
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---rabbit---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = TOPIC_1)
    public void receiveMessage2(User message) {
        logger.info("receiveMessage---rabbit---2=" + message.getName());
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = TOPIC_DEAD)
    public void mqDLQ(String message) {
        logger.info("rabbit.DLQ=" + message);
    }
    
    @MqListener(name = TOPIC_1_DEAD)
    public void mqDLQ1(String message) {
        logger.info("rabbit1.DLQ=" + message);
    }
    
    @GetMapping("/mq/rabbit/sendMessage")
    public Object sendRabbitMessage() {
        User user = getUser();
        user.setName("张三" + RandomUtil.nextInt());
        try {
            boolean flag = mqProducer.getProducer(TOPIC).syncSend(user);
            logger.info("rabbit.send->{}", flag);

            mqProducer.getProducer(TOPIC_1).asyncSend(user, new SendCallback() {
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
//            mqFactory.getMqTemplate(mqProducer.getBinder(TOPIC)).syncSend(TOPIC, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @GetMapping("/mq/rabbit/sendDelayMessage")
    public Object sendRabbitDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(TOPIC).syncDelaySend(user, 3, TimeUnit.SECONDS);
            logger.info("rabbit.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
