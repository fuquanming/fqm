package com.fqm.test.mq.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.test.mq.model.User;

@RestController
public class RedisMqController extends MqController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    @Value("${mq.mqs.c.binder:}")
    private String mqBinder;
    @Value("${mq.mqs.c1.binder:}")
    private String mqBinder1;
    
    @Value("${mq.mqs.c.topic:}")
    private String topic;
    @Value("${mq.mqs.c1.topic:}")
    private String topic1;

    @MqListener(name = "${mq.mqs.c.name}")
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---redis---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = "${mq.mqs.c1.name}")
    public void receiveMessage2(String message) {
        logger.info("receiveMessage---redis---2=" + message);
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = "${mq.mqs.c-dead.name}")
    public void mqDLQ(String message) {
        logger.info("redis.DLQ=" + message);
    }
    @MqListener(name = "${mq.mqs.c1-dead.name}")
    public void mqDLQ1(String message) {
        logger.info("redis1.DLQ=" + message);
    }

    @GetMapping("/mq/redis/sendMessage")
    public Object sendRedisMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(mqBinder).syncSend(topic, user);
            logger.info("redis.send->{}", flag);
            mqFactory.getMqTemplate(mqBinder).syncSend(topic1, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @GetMapping("/mq/redis/sendDelayMessage")
    public Object sendRedisDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(MqMode.REDIS).syncDelaySend(topic, user, 5, TimeUnit.SECONDS);
            logger.info("redis.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
