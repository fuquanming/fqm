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
import com.fqm.framework.mq.config.MqProducer;
import com.fqm.test.controller.BaseController;
import com.fqm.test.model.User;

@RestController
public class KafkaMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    /** 业务名称：对应配置文件 mq.mqs.xxx */
    public static final String BUSINESS_NAME = "kafka-topic";
    public static final String BUSINESS_NAME_1 = "kafka-topic1";
    /** 死信业务名称：对应配置文件 mq.mqs.xxx，死信主题：topic + ".DLT" */
    public static final String BUSINESS_NAME_DEAD = BUSINESS_NAME + "-dead";
    public static final String BUSINESS_NAME_1_DEAD = BUSINESS_NAME_1 + "-dead";
    @Resource
    MqProducer mqProducer;

    @MqListener(name = BUSINESS_NAME)
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---kafka---1={}", message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }
    
    @MqListener(name = BUSINESS_NAME_1)
    public void receiveMessage2(User message) {
        logger.info("receiveMessage---kafka---2={}", message.getName());
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = BUSINESS_NAME_DEAD)
    public void mqDLQ(String message) {
        logger.info("kafka.DLQ={}", message);
    }
    @MqListener(name = BUSINESS_NAME_1_DEAD)
    public void mqDLQ1(String message) {
        logger.info("kafka1.DLQ={}", message);
    }
    
    @GetMapping("/mq/kafka/sendMessage")
    public Object sendKafkaMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_NAME).syncSend(user);
            logger.info("kafka.send->{}", flag);

            mqProducer.getProducer(BUSINESS_NAME_1).asyncSend(user, new SendCallback() {
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
//            mqFactory.getMqTemplate(mqProducer.getBinder(BUSINESS_NAME)).syncSend(mqProducer.getTopic(BUSINESS_NAME), user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
