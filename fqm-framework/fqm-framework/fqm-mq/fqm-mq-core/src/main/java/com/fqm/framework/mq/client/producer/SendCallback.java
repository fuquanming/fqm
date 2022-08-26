package com.fqm.framework.mq.client.producer;
/**
 * 生产者发送消息回调
 * 
 * @version 
 * @author 傅泉明
 */
public interface SendCallback {
    /**
     * 发送成功 
     * @param sendResult
     */
    void onSuccess(final SendResult sendResult);

    /**
     * 发送失败
     * @param e
     */
    void onException(final Throwable e);
}
