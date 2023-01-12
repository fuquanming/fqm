/*
 * @(#)EmqxClient.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-mq-emqx
 * 创建日期 : 2022年11月23日
 * 修改历史 : 
 *     1. [2022年11月23日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.fqm.framework.common.core.util.NetUtil;
import com.fqm.framework.common.core.util.system.SystemUtil;

/**
 * Emqx 客户端
 * @version 
 * @author 傅泉明
 */
public class EmqxClient {
    /** 缓存每个监听主题的序号，用来创建客户端，每个客户端监听一个主题 */
    private static final Map<String, AtomicInteger> TOPIC_MAP = new ConcurrentHashMap<>();

    /** Emqx服务端：tcp://127.0.0.1:1883 */
    private String connectString;
    /** Emqx用户名 */
    private String username;
    /** Emqx密码 */
    private String password;
    /** 
     * 会话持久化消息，false 保存(clientId 不能变)；true 不保存。会话被销毁则消息被丢弃。
     * 是否清空session，必须clientId不能变，设置false表示服务器会保留客户端的连接记录（订阅主题，qos）,客户端重连之后能获取到服务器在客户端断开连接期间推送的消息 
     **/
    private boolean cleanSession = false;
    /** 连接超时时间，默认30秒 */
    private int connectionTimeoutSecond = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
    /** 设置心跳时间 单位为秒，默认30秒 */
    private int keepAliveIntervalSecond = 30;
    
    private MqttClient client;
    /** 监听消息主题 */
    private String topic;
    
    /**
     * 发消息的客户端，不保存会话，clientId= mac地址+进程pid
     * @param connectString Emqx服务端：tcp://127.0.0.1:1883
     * @throws MqttException
     */
    public EmqxClient(String connectString) throws MqttException {
        this.connectString = connectString;
        // 发消息，clientId= mac地址+进程pid
        String clientId = NetUtil.getMacAddress(NetUtil.getLocalhost(), "") + ":" + SystemUtil.getCurrentPid();
        client = new MqttClient(connectString, clientId);
    }
    
    /**
     * 监听消息的客户端，保存会话，clientId= mac地址+端口号+主题+i（自增）
     * @param connectString Emqx服务端：tcp://127.0.0.1:1883
     * @param callback          回调方法
     * @param topic             监听的主题
     * @param port              应用程序的端口号
     * @throws MqttException
     */
    public EmqxClient(String connectString, MqttCallback callback, String topic, String port) throws MqttException {
        this.connectString = connectString;
        // 监听消息，clientId= mac地址+端口号+主题+i（自增）
        AtomicInteger atomicInteger = TOPIC_MAP.get(topic);
        if (null == atomicInteger) {
            atomicInteger = new AtomicInteger();
        }
        int topicIndex = atomicInteger.incrementAndGet();
        TOPIC_MAP.put(topic, atomicInteger);
        
        String clientId = NetUtil.getMacAddress(NetUtil.getLocalhost(), "") + ":" + port + ":" + topic + ":" + topicIndex;
        client = new MqttClient(connectString, clientId);
        if (null != callback) {
            //设置回调
            client.setCallback(callback);
            this.topic = topic;
        }
    }
    
    public MqttClient getMqttClient() {
        return this.client;
    }
    
    public MqttClient connect() throws MqttException {
        if (null != client) {
            //连接设置
            MqttConnectOptions options = new MqttConnectOptions();
            // 监听消息的客户端：开启手动确认消息、不清除会话
            // 发消息的客户端，清除会话，没有回调函数、没有手动确认消息
            if (null != this.topic) {
                // clean_session=false）时，订阅者可以在重新连接后立即恢复数据流而不丢失消息
                //是否清空session，必须clientId不能变，设置false表示服务器会保留客户端的连接记录（订阅主题，qos）,客户端重连之后能获取到服务器在客户端断开连接期间推送的消息
                //设置为true表示每次连接服务器都是以新的身份，
                options.setCleanSession(cleanSession);
                //设置手动消息接收确认
                client.setManualAcks(true);
            }
            
            //设置连接用户名
            options.setUserName(username);
            //设置连接密码
            options.setPassword(password.toCharArray());
            //设置超时时间，单位为秒
            options.setConnectionTimeout(connectionTimeoutSecond);
            //设置心跳时间 单位为秒，表示服务器每隔 1.5*20秒的时间向客户端发送心跳判断客户端是否在线
            options.setKeepAliveInterval(keepAliveIntervalSecond);
            //是否自动重新连接
            options.setAutomaticReconnect(true);
            //设置遗嘱消息的话题，若客户端和服务器之间的连接意外断开，服务器将发布客户端的遗嘱信息
            options.setWill("willTopic", (client.getClientId() + "与服务器断开连接").getBytes(), 0, false);
            
            client.connect(options);
        }
        return client;
    }
    
    public void destroy() throws MqttException {
        if (null != client) {
            client.disconnect();
        }
    }
    
    public String getConnectString() {
        return connectString;
    }
    public EmqxClient setConnectString(String connectString) {
        this.connectString = connectString;
        return this;
    }
    public String getUsername() {
        return username;
    }
    public EmqxClient setUsername(String username) {
        this.username = username;
        return this;
    }
    public String getPassword() {
        return password;
    }
    public EmqxClient setPassword(String password) {
        this.password = password;
        return this;
    }
    public boolean getCleanSession() {
        return cleanSession;
    }
    public EmqxClient setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }
    public int getConnectionTimeoutSecond() {
        return connectionTimeoutSecond;
    }
    public EmqxClient setConnectionTimeoutSecond(int connectionTimeoutSecond) {
        this.connectionTimeoutSecond = connectionTimeoutSecond;
        return this;
    }
    public int getKeepAliveIntervalSecond() {
        return keepAliveIntervalSecond;
    }
    public EmqxClient setKeepAliveIntervalSecond(int keepAliveIntervalSecond) {
        this.keepAliveIntervalSecond = keepAliveIntervalSecond;
        return this;
    }
    
}
