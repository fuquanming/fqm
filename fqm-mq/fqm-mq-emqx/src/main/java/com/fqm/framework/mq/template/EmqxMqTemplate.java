package com.fqm.framework.mq.template;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;

/**
 * Emqx消息队列，基于 MQTT 协议
 * 
 * @version 
 * @author 傅泉明
 */
public class EmqxMqTemplate implements MqTemplate {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private MqttClient client = null;
    
    private String messageStr = "],message=[";

    public EmqxMqTemplate(MqttClient client) {
        this.client = client;
    }
    
    @Override
    public MqMode getMqMode() {
        return MqMode.EMQX;
    }

    /**
     * 同步发送消息（在发消息前，没有被订阅，则该消息不会被消费）
     * @see com.fqm.framework.mq.template.MqTemplate#syncSend(java.lang.String, java.lang.Object)
     */
    @Override
    public boolean syncSend(String topic, Object msg) {
        String str = getJsonStr(msg);
        MqttMessage mqttMessage = new MqttMessage();
        // qos 默认：1；retained 默认：false
        mqttMessage.setPayload(str.getBytes(StandardCharsets.UTF_8));
        //主题的目的地，用于发布/订阅信息
        MqttTopic mqttTopic = client.getTopic(topic);
        //提供一种机制来跟踪消息的传递进度
        //用于在以非阻塞方式（在后台运行）执行发布是跟踪消息的传递进度
        MqttDeliveryToken token;
        try {
            //将指定消息发布到主题，但不等待消息传递完成，返回的token可用于跟踪消息的传递状态
            token = mqttTopic.publish(mqttMessage);
            token.waitForCompletion();
            logger.info("syncSend.success->topic=[{}],message=[{}]", topic, str);
            return true;
        } catch (MqttException e) {
            logger.error("syncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public void asyncSend(String topic, Object msg, SendCallback sendCallback) {
        String str = getJsonStr(msg);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(str.getBytes(StandardCharsets.UTF_8));
        MqttTopic mqttTopic = client.getTopic(topic);
        MqttDeliveryToken token;
        try {
            token = mqttTopic.publish(mqttMessage);
            // 第一次发送消息，不会执行回调函数？
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    sendCallback.onSuccess(new SendResult().setId(String.valueOf(asyncActionToken.getMessageId())));
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    sendCallback.onException(exception);
                }
            });
            logger.info("asyncSend.success->topic=[{}],message=[{}]", topic, str);
        } catch (MqttException e) {
            logger.error("asyncSend.error->topic=[" + topic + messageStr + str + "]", e);
            e.printStackTrace();
        }
    }

    @Override
    public boolean syncDelaySend(String topic, Object msg, int delayTime, TimeUnit timeUnit) {
        String str = getJsonStr(msg);
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(str.getBytes(StandardCharsets.UTF_8));
        String delayTopic = "$delayed/" + TimeUnit.SECONDS.toSeconds(delayTime) + "/" + topic;
        MqttTopic mqttTopic = client.getTopic(delayTopic);
        MqttDeliveryToken token;
        try {
            token = mqttTopic.publish(mqttMessage);
            token.waitForCompletion();
            logger.info("syncDelaySend.success->topic=[{}],message=[{}],delayTime=[{}],timeUnit=[{}]", topic, str, delayTime, timeUnit);
            return true;
        } catch (MqttException e) {
            logger.error("syncDelaySend.error->topic=[" + topic + messageStr + str + "],delayTime=[" + delayTime + "],timeUnit=[" + timeUnit + "]", e);
            e.printStackTrace();
        }
        return false;
    }
}
