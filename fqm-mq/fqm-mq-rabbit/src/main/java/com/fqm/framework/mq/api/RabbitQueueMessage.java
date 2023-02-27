package com.fqm.framework.mq.api;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * RabbitMQ 队列查询消息
 * @version 
 * @author 傅泉明
 */
public class RabbitQueueMessage {
    /** 消息序号：0为最后一条 */
    @JsonAlias("message_count")
    private Integer messageCount;
    /** 消息内容：json格式的字符串 */
    private String payload;
    /** 消息编码：string */
    @JsonAlias("payload_encoding")
    private String payloadEncoding;
    public Integer getMessageCount() {
        return messageCount;
    }
    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
    public String getPayload() {
        return payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }
    public String getPayloadEncoding() {
        return payloadEncoding;
    }
    public void setPayloadEncoding(String payloadEncoding) {
        this.payloadEncoding = payloadEncoding;
    }
    
}
