package com.fqm.test.mq.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;
import com.fqm.test.controller.BaseController;
import com.fqm.test.model.User;

@RestController
public class KafkaMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    @Value("${mq.mqs.a.binder:}")
    private String mqBinder;
    @Value("${mq.mqs.a1.binder:}")
    private String mqBinder1;
    
    @Value("${mq.mqs.a.topic:}")
    private String topic;
    @Value("${mq.mqs.a1.topic:}")
    private String topic1;

    @MqListener(name = "${mq.mqs.a.name}")
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---kafka---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }
    
    @MqListener(name = "${mq.mqs.a1.name}")
    public void receiveMessage2(String message) {
        logger.info("receiveMessage---kafka---2=" + message);
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = "${mq.mqs.a-dead.name}")
    public void mqDLQ(String message) {
        logger.info("kafka.DLQ=" + message);
    }
    @MqListener(name = "${mq.mqs.a1-dead.name}")
    public void mqDLQ1(String message) {
        logger.info("kafka1.DLQ=" + message);
    }

    @GetMapping("/mq/kafka/sendMessage")
    public Object sendKafkaMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(mqBinder).syncSend(topic, user);
            logger.info("kafka.send->{}", flag);

            mqFactory.getMqTemplate(MqMode.KAFKA).asyncSend(topic1, user, new SendCallback() {
                public void onSuccess(SendResult sendResult) {
                    logger.info("SendResult success,");
                }

                public void onException(Throwable e) {
                    logger.info("SendResult onException," + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
