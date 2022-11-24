/*
 * @(#)EmqxProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-mq-emqx
 * 创建日期 : 2022年11月21日
 * 修改历史 : 
 *     1. [2022年11月21日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Emqx配置
 * @version 
 * @author 傅泉明
 */
public class EmqxProperties {
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
    
    public String getConnectString() {
        return connectString;
    }
    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean getCleanSession() {
        return cleanSession;
    }
    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }
    public int getConnectionTimeoutSecond() {
        return connectionTimeoutSecond;
    }
    public void setConnectionTimeoutSecond(int connectionTimeoutSecond) {
        this.connectionTimeoutSecond = connectionTimeoutSecond;
    }
    public int getKeepAliveIntervalSecond() {
        return keepAliveIntervalSecond;
    }
    public void setKeepAliveIntervalSecond(int keepAliveIntervalSecond) {
        this.keepAliveIntervalSecond = keepAliveIntervalSecond;
    }
}
