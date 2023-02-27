package com.fqm.framework.mq.api;

/**
 * RabbitMQ 队列查询消息请求
 * @version 
 * @author 傅泉明
 */
public class RabbitQueueMessageQueryRequest {

    private String vhost = "/";
    private String name;
    private String truncate = "50000";
    private String ackmode = "ack_requeue_true";
    private String encoding = "auto";
    private String count;
    
    public String getVhost() {
        return vhost;
    }
    public void setVhost(String vhost) {
        this.vhost = vhost;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTruncate() {
        return truncate;
    }
    public void setTruncate(String truncate) {
        this.truncate = truncate;
    }
    public String getAckmode() {
        return ackmode;
    }
    public void setAckmode(String ackmode) {
        this.ackmode = ackmode;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }
    
}
