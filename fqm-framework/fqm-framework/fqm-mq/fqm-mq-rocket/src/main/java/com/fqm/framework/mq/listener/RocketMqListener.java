package com.fqm.framework.mq.listener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.spring.core.RocketMQListener;

import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.mq.template.RocketMqTemplate;
import com.fqm.framework.mq.util.RocketDelayUtil;

/**
 * Rocket消息队列监听
 * 
 * @version 
 * @author 傅泉明
 */
public class RocketMqListener extends MqListenerAdapter<String> implements RocketMQListener<String> {

    private String topic;
    private RocketMqTemplate rocketMqTemplate;
    
    public RocketMqListener(Object bean, Method method, String topic, RocketMqTemplate rocketMqTemplate) {
        super(method, bean);
        this.rocketMqTemplate = rocketMqTemplate;
        this.topic = topic;
    }
    
    @Override
    public void onMessage(String message) {
        try {
            // 一、延迟时间存在十八个等级 (1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h )
            // 通过18级的时间，每次比较最近所在的级别时间投递进去。
            int delayTime = RocketDelayUtil.getNextDelaySecond(message);
            if (delayTime == -1) {
                // 不是延迟
                super.receiveMessage(message);
            } else if (delayTime == 0) {
                // 不再延迟
                super.receiveMessage(RocketDelayUtil.buildRemoveDelayMsgJson(message));
            } else {
                // 延迟
                rocketMqTemplate.syncDelaySend(topic, JsonUtil.toMap(message), delayTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
