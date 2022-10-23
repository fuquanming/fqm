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
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;
import com.fqm.test.mq.model.User;

@RestController
public class RedissonMqController extends MqController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    @Value("${mq.mqs.d.binder:}")
    private String mqBinder;
    @Value("${mq.mqs.d1.binder:}")
    private String mqBinder1;
    
    @Value("${mq.mqs.d.topic:}")
    private String topic;
    @Value("${mq.mqs.d1.topic:}")
    private String topic1;
    
    @MqListener(name = "${mq.mqs.d.name}")
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---redisson---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }

    @MqListener(name = "${mq.mqs.d1.name}")
    public void receiveMessage2(String message) {
        logger.info("receiveMessage---redisson---2=" + message);
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

    @MqListener(name = "${mq.mqs.d-dead.name}")
    public void mqDLQ(String message) {
        logger.info("redisson.DLQ=" + message);
    }
    
    @MqListener(name = "${mq.mqs.d1-dead.name}")
    public void mqDLQ1(String message) {
        logger.info("redisson1.DLQ=" + message);
    }

    @GetMapping("/mq/redisson/sendMessage")
    public Object sendRedisMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(mqBinder).syncSend(topic, user);
            logger.info("redisson.send->{}", flag);
            
            mqFactory.getMqTemplate(MqMode.REDISSON).asyncSend(topic1, user, new SendCallback() {
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

    @GetMapping("/mq/redisson/sendDelayMessage")
    public Object sendRedisDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(MqMode.REDISSON).syncDelaySend(topic, user, 5, TimeUnit.SECONDS);
            logger.info("redisson.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
    
    @GetMapping("/mq/redisson/test")
    public Object test() {
//        System.out.println(redisProperties);
        return null;
//        List<Map<String, Object>> list = new ArrayList<>();
//
//        String[] beans = SpringUtil.getApplicationContext().getBeanDefinitionNames();
//
//        for (String beanName : beans) {
//            Class<?> beanType = SpringUtil.getApplicationContext()
//                    .getType(beanName);
//
//            Map<String, Object> map = new HashMap<>();
//
//            map.put("BeanName", beanName);
//            map.put("beanType", beanType);
//            map.put("package", beanType.getPackage());
//            list.add(map);
//        }
//        return list;
    }

}
