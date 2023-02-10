package com.fqm.test.mq.controller;

import java.util.concurrent.TimeUnit;

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
public class RocketMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    /** 业务名称：对应配置文件 mq.mqs.xxx */
    public static final String BUSINESS_NAME = "rocket-topic";
    public static final String BUSINESS_NAME_1 = "rocket-topic1";
    /** 死信业务名称：对应配置文件 mq.mqs.xxx，死信主题：%DLQ% + 消费组，死信主题需要在rocket控制台：主题->勾选死信->点击“TOPIC配置”->修改 perm 值，修改为6（可读可写权限） */
    public static final String BUSINESS_NAME_DEAD = BUSINESS_NAME + "-dead";
    public static final String BUSINESS_NAME_1_DEAD = BUSINESS_NAME_1 + "-dead";
    @Resource
    MqProducer mqProducer;

    @MqListener(name = BUSINESS_NAME)
    public void receiveMessage1(String message) {
        logger.info("receiveMessage---rocket---1={}", message);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }
    
    @MqListener(name = BUSINESS_NAME_1)
    public void receiveMessage2(User message) {
        logger.info("receiveMessage---rocket---2={}", message.getName());
//        if (true) {
//            throw new RuntimeException("error 222");
//        }
    }

//     死信队列需要在rocket控制台：主题->勾选死信->点击“TOPIC配置”->修改 perm 值，修改为6（可读可写权限）
    @MqListener(name = BUSINESS_NAME_DEAD)
    public void mqDLQ(String message) {
        logger.info("rocket.DLQ={}", message);
    }
    @MqListener(name = BUSINESS_NAME_1_DEAD)
    public void mqDLQ1(String message) {
        logger.info("rocket1.DLQ={}", message);
    }

    @GetMapping("/mq/rocket/sendMessage")
    public Object sendrocketMessage() {
        User user = getUser();
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_NAME).syncSend(user);
            logger.info("rocket.send->{}", flag);
            
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
    
    @GetMapping("/mq/rocket/sendDelayMessage")
    public Object sendRocketDelayMessage() {
        User user = getDelayUser();
        try {
            boolean flag = mqProducer.getProducer(BUSINESS_NAME).syncDelaySend(user, 3, TimeUnit.SECONDS);
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
