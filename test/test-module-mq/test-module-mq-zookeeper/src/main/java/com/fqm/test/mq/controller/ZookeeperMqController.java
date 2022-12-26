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
public class ZookeeperMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    
    public static final String BUSINESS_CREATE_ORDER = "f";
    public static final String BUSINESS_CREATE_ORDER_1 = "f1";
    public static final String BUSINESS_CREATE_ORDER_DEAD = "f-dead";
    public static final String BUSINESS_CREATE_ORDER_DEAD_1 = "f1-dead";
    @Resource
    MqProducer mqProducer;

    @MqListener(name = BUSINESS_CREATE_ORDER)
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---zookeeper---1={}", message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = BUSINESS_CREATE_ORDER_1)
    public void receiveMessage2(User message) {
        logger.info("receiveMessage---zookeeper---2={}", message.getName());
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }
    
    @MqListener(name = BUSINESS_CREATE_ORDER_DEAD)
    public void mqDLQ(String message) {
        logger.info("zookeeper.DLQ={}", message);
    }
    
    @MqListener(name = BUSINESS_CREATE_ORDER_DEAD_1)
    public void mqDLQ1(String message) {
        logger.info("zookeeper1.DLQ={}", message);
    }

    @GetMapping("/mq/zookeeper/sendMessage")
    public Object sendZookeeperMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_CREATE_ORDER).syncSend(user);
            logger.info("zookeeper.send->{}", flag);
            
            mqProducer.getProducer(BUSINESS_CREATE_ORDER_1).syncSend(user);
            // 通过消息模板发送消息
//            mqFactory.getMqTemplate(mqProducer.getBinder(BUSINESS_CREATE_ORDER_1))
//                .syncSend(mqProducer.getTopic(BUSINESS_CREATE_ORDER_1), user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
    
    @GetMapping("/mq/zookeeper/sendDelayMessage")
    public Object sendZookeeperDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_CREATE_ORDER).syncDelaySend(user, 3, TimeUnit.SECONDS);
            logger.info("zookeeper.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
