package com.fqm.test.mq.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;
import com.fqm.test.controller.BaseController;
import com.fqm.test.model.User;
import com.fqm.test.mq.config.EmqxMqProperties;

@RestController
@EnableConfigurationProperties(EmqxMqProperties.class)
public class EmqxMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    @Value("${mq.mqs.g.binder:}")
    private String mqBinder;
    @Value("${mq.mqs.g1.binder:}")
    private String mqBinder1;
    
    @Value("${mq.mqs.g.topic:}")
    private String topic;
    @Value("${mq.mqs.g1.topic:}")
    private String topic1;

    @Resource
    EmqxMqProperties emqxMqProperties;

    @MqListener(name = "${mq.mqs.g.name}")
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---emqx---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = "${mq.mqs.g1.name}")
    public void receiveMessage2(String message) {
        logger.info("receiveMessage---emqx---2=" + message);
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }
    
    @MqListener(name = "${mq.mqs.g-dead.name}")
    public void mqDLQ(String message) {
        logger.info("emqx.DLQ=" + message);
    }
    
    @MqListener(name = "${mq.mqs.g1-dead.name}")
    public void mqDLQ1(String message) {
        logger.info("emqx1.DLQ=" + message);
    }

    @GetMapping("/mq/emqx/sendMessage")
    public Object sendEmqxMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(emqxMqProperties.getBinder()).syncSend(emqxMqProperties.getTopic(), user);
            logger.info("emqx.send->{}", flag);
            
            mqFactory.getMqTemplate(mqBinder1).asyncSend(topic1, user, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("onSuccess");
                }
                
                @Override
                public void onException(Throwable e) {
                    System.out.println("onException");
                }
            });
//            logger.info("emqx.send->{}", flag1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
    
    @GetMapping("/mq/emqx/sendDelayMessage")
    public Object sendEmqxDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(MqMode.EMQX).syncDelaySend(topic, user, 3, TimeUnit.SECONDS);
            logger.info("emqx.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}