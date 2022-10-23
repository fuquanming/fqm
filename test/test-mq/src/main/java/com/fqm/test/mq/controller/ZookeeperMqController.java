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
public class ZookeeperMqController extends MqController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    @Value("${mq.mqs.f.binder:}")
    private String mqBinder;
    @Value("${mq.mqs.f1.binder:}")
    private String mqBinder1;
    
    @Value("${mq.mqs.f.topic:}")
    private String topic;
    @Value("${mq.mqs.f1.topic:}")
    private String topic1;

    @MqListener(name = "${mq.mqs.f.name}")
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---zookeeper---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = "${mq.mqs.f1.name}")
    public void receiveMessage2(String message) {
        logger.info("receiveMessage---zookeeper---2=" + message);
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }
    
    @MqListener(name = "${mq.mqs.f-dead.name}")
    public void mqDLQ(String message) {
        logger.info("zookeeper.DLQ=" + message);
    }
    
    @MqListener(name = "${mq.mqs.f1-dead.name}")
    public void mqDLQ1(String message) {
        logger.info("zookeeper1.DLQ=" + message);
    }

    @GetMapping("/mq/zookeeper/sendMessage")
    public Object sendZookeeperMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(mqBinder).syncSend(topic, user);
            logger.info("zookeeper.send->{}", flag);
            
            boolean flag1 = mqFactory.getMqTemplate(mqBinder1).syncSend(topic1, user);
            logger.info("zookeeper.send->{}", flag1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
    
    @GetMapping("/mq/zookeeper/sendDelayMessage")
    public Object sendZookeeperDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(MqMode.ZOOKEEPER).syncDelaySend(topic, user, 5, TimeUnit.SECONDS);
            logger.info("zookeeper.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
