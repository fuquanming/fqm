package com.fqm.test.mq.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.util.MqProducer;
import com.fqm.test.controller.BaseController;
import com.fqm.test.model.User;

@RestController
public class RedisMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    /** 消息主题名称：对应配置文件 mq.mqs.xxx */
    public static final String TOPIC = "redis-topic";
    public static final String TOPIC_1 = "redis-topic1";
    /** 死信主题名称：对应配置文件 mq.mqs.xxx，死信主题：group + ".DLQ" */
    public static final String TOPIC_DEAD = "msg-group.DLQ";
    public static final String TOPIC_1_DEAD = "msg-group-1.DLQ";
    @Resource
    MqProducer mqProducer;

    @MqListener(name = TOPIC)
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---redis---1={}", message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = TOPIC_1)
    public void receiveMessage2(User message) {
        logger.info("receiveMessage---redis---2={}", message.getName());
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = TOPIC_DEAD)
    public void mqDLQ(String message) {
        logger.info("redis.DLQ={}", message);
    }
    @MqListener(name = TOPIC_1_DEAD)
    public void mqDLQ1(String message) {
        logger.info("redis1.DLQ={}", message);
    }
    
    @GetMapping("/mq/redis/sendMessage")
    public Object sendRedisMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(TOPIC).syncSend(user);
            logger.info("redis.send->{}", flag);
            mqProducer.getProducer(TOPIC_1).syncSend(user);
            // 通过消息模板发送消息
//            mqFactory.getMqTemplate(mqProducer.getBinder(TOPIC)).syncSend(TOPIC, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @GetMapping("/mq/redis/sendDelayMessage")
    public Object sendRedisDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(TOPIC).syncDelaySend(user, 3, TimeUnit.SECONDS);
            logger.info("redis.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
