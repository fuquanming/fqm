package com.fqm.framework.mq.config;
/**
 * Zookeeper配置
 * 
 * @version 
 * @author 傅泉明
 */
public class ZookeeperProperties {

    /** zookeeper服务端 */
    private String connectString;

    /** 会话超时时间 */
    private int sessionTimeout = 30000;

    /** 连接超时时间 */
    private int connectionTimeout = 15000;

    /** 初始休眠时间 */
    private int baseSleepTimeMs = 1000;

    /** 最大重试次数 */
    private int maxRetries = 3;

    /** 最大休眠时间 */
    private int maxSleepMs = 10000;

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxSleepMs() {
        return maxSleepMs;
    }

    public void setMaxSleepMs(int maxSleepMs) {
        this.maxSleepMs = maxSleepMs;
    }
    
}
