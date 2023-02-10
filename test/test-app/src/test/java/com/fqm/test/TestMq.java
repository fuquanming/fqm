package com.fqm.test;

import com.fqm.framework.mq.MqMode;

import cn.hutool.http.HttpUtil;

public class TestMq {

    static String serverUrl = "http://127.0.0.1:18080/";

    static String mqMessageUrl = "mq/%s/sendMessage";

    static String mqDelayMessageUrl = "mq/%s/sendDelayMessage";

    public static String getSendMessageUrl(MqMode mqMode) {
        return serverUrl + String.format(mqMessageUrl, mqMode.name().toLowerCase());
    }

    public static String getSendDelayMessageUrl(MqMode mqMode) {
        return serverUrl + String.format(mqDelayMessageUrl, mqMode.name().toLowerCase());
    }

    public static void testMq(MqMode mqMode) {
        String sendMessageUrl = getSendMessageUrl(mqMode);
        String sendDelayMessageUrl = getSendDelayMessageUrl(mqMode);
        HttpUtil.get(sendMessageUrl);
        System.out.println(sendMessageUrl);
        if (mqMode != MqMode.KAFKA) {
            HttpUtil.get(sendDelayMessageUrl);
            System.out.println(sendDelayMessageUrl);
        }
    }

    public static void main(String[] args) {
        testMq(MqMode.EMQX);
        testMq(MqMode.KAFKA);
        testMq(MqMode.RABBIT);
        testMq(MqMode.REDIS);
        testMq(MqMode.REDISSON);
        testMq(MqMode.ROCKET);
        testMq(MqMode.ZOOKEEPER);
    }

}
