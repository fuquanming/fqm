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
public class RocketMqController extends MqController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    @Value("${mq.mqs.e.binder:}")
    private String mqBinder;
    @Value("${mq.mqs.e1.binder:}")
    private String mqBinder1;
    
    @Value("${mq.mqs.e.topic:}")
    private String topic;
    @Value("${mq.mqs.e1.topic:}")
    private String topic1;

    @MqListener(name = "${mq.mqs.e.name}")
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---rocket---1=" + message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }
    
    @MqListener(name = "${mq.mqs.e1.name}")
    public void receiveMessage2(String message) {
        logger.info("receiveMessage---rocket---2=" + message);
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

//     死信队列需要在rocket控制台：主题->勾选死信->点击“TOPIC配置”->修改 perm 值，修改为6（可读可写权限）
    @MqListener(name = "${mq.mqs.e-dead.name}")
    public void mqDLQ(String message) {
        logger.info("rocket.DLQ=" + message);
    }
    @MqListener(name = "${mq.mqs.e1-dead.name}")
    public void mqDLQ1(String message) {
        logger.info("rocket1.DLQ=" + message);
    }

    @GetMapping("/mq/rocket/sendMessage")
    public Object sendrocketMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(mqBinder).syncSend(topic, user);
            logger.info("rocket.send->{}", flag);
            mqFactory.getMqTemplate(MqMode.ROCKET).asyncSend(topic1, user, new SendCallback() {
                public void onSuccess(SendResult sendResult) {
                    logger.info("SendResult success," + sendResult.getId());
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
    
    @GetMapping("/mq/rocket/sendDelayMessage")
    public Object sendRocketDelayMessage() {
        User user = getUser();
        try {
            boolean flag = mqFactory.getMqTemplate(MqMode.ROCKET).syncDelaySend(topic, user, 5, TimeUnit.SECONDS);
            logger.info("rocket.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        name=rocketListener.my-topic.RocketMqController.receiveMessage1
//                name=rocketListener.my-topic.RocketMqController.receiveMessage2
//        DefaultRocketMQListenerContainer m1 = (DefaultRocketMQListenerContainer)SpringUtil.getBean("rocketListener.my-topic.RocketMqController.receiveMessage1");
//        DefaultRocketMQListenerContainer m2 = (DefaultRocketMQListenerContainer)SpringUtil.getBean("rocketListener.my-topic.RocketMqController.receiveMessage2");
        // DefaultMQPushConsumerImpl 设置暂停
        // PullMessageService 中拉取任务
//        final MQConsumerInner consumer = this.mQClientFactory.selectConsumer(pullRequest.getConsumerGroup());
//        if (consumer != null) {
//            DefaultMQPushConsumerImpl impl = (DefaultMQPushConsumerImpl) consumer;
//            impl.pullMessage(pullRequest);
//        } else {
//            log.warn("No matched consumer for the PullRequest {}, drop it", pullRequest);
//        }
        
//        Field pauseField = FieldUtils.getField(DefaultMQPushConsumerImpl.class, "pause", true);
//        System.out.println(pauseField.getBoolean(obj));
        
//        System.out.println(m1.getConsumer().getDefaultMQPushConsumerImpl().isPause());
//        try {
//            DefaultMQPushConsumerImpl pc = (DefaultMQPushConsumerImpl)FieldUtils.readDeclaredField(m1.getConsumer(), "defaultMQPushConsumerImpl", true);
//            if (pc != null) {
//                pc.setPause(true);// 暂停消费，可以恢复运行
//                pc.setPause(false);
//                // 获取拉取消息的服务
////                PullMessageService pms = pc.getmQClientFactory().getPullMessageService();
////                System.out.println("pms=" + pms.isStopped());
////                pms.makeStop();// 暂停拉取消息服务，修改后不能恢复运行。。。
////                FieldUtils.writeDeclaredField((ServiceThread)pms, "stopped", false, true);
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
////        m1.getConsumer().getDefaultMQPushConsumerImpl().setPause(true);
//        System.out.println(m1.getConsumer().getDefaultMQPushConsumerImpl().isPause());
        
//        System.out.println(m1);
//        System.out.println(m2);
        return user;
    }

}
