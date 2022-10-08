package com.fqm.framework.mq.client.producer;
/**
 * 发送消息的结果
 * 
 * @version 
 * @author 傅泉明
 */
public class SendResult {

    private String id;

    public String getId() {
        return id;
    }

    public SendResult setId(String id) {
        this.id = id;
        return this;
    }
    
}
