/*
 * @(#)ProxyInfo.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年3月18日
 * 修改历史 : 
 *     1. [2021年3月18日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.http;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class ProxyInfo {
    private String ip;
    private int port;
    public String getIp() {
        return ip;
    }
    public ProxyInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }
    public int getPort() {
        return port;
    }
    public ProxyInfo setPort(int port) {
        this.port = port;
        return this;
    }
}
