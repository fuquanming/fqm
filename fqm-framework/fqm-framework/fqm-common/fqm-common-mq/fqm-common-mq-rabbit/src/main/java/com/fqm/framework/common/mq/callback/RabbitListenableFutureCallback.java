package com.fqm.framework.common.mq.callback;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData.Confirm;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fqm.framework.common.mq.client.producer.SendCallback;
import com.fqm.framework.common.mq.client.producer.SendResult;

/**
 * 设置确认回调
 * 
 * @version 
 * @author 傅泉明
 */
public class RabbitListenableFutureCallback implements ListenableFutureCallback<Confirm> {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    /** 生产者线程 */
    private Thread productThread;
    /** 消息ID */
    private String id;
    /** 是否异常 */
    private volatile Boolean msgError = false;
    /** ack回调线程 */
    private Thread callbackThread;
    
    private SendCallback sendCallback;
    
    public RabbitListenableFutureCallback(Thread productThread, String id) {
        this.productThread = productThread;
        this.id = id;
    }
    
    public RabbitListenableFutureCallback(Thread productThread, String id, SendCallback sendCallback) {
        this.productThread = productThread;
        this.id = id;
        this.sendCallback = sendCallback;
    }
    
    @Override
    public void onFailure(Throwable ex) {
        logger.error("Throwable", ex);
        // 设置确认回调失败时触发
        msgError = true;
        LockSupport.unpark(productThread);
        if (sendCallback != null) {// 同步消息
            sendCallback.onException(ex);
        }
    }
    @Override
    public void onSuccess(Confirm confirm) {
        // @RabbitPropertiesBeanPostProcessor 参考
        // 设置确认回调 ACK
        callbackThread = Thread.currentThread();
        logger.info(callbackThread.getId() + ",onSuccess,ack=" + confirm.isAck() + "\t" + productThread.getId());
        if (!confirm.isAck()) {// 入交换机失败
            msgError = true;
        }
        // 等待入队列异常通知，最多等待1毫秒 
        // TODO 性能，在无异常时白损耗1毫秒
        if (sendCallback == null) {// 同步消息
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
            if (msgError) {// 入队列异常
                logger.info("msgError=" + msgError);
            } else if (confirm.isAck()) {
                // 消息ack成功，队列成功
                // 业务处理
            }
            LockSupport.unpark(productThread);
        } else {// 异步消息
            if (msgError) {// 入交换机失败
                sendCallback.onException(new RuntimeException("ERROR exchange"));
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2));
            if (confirm.isAck() && !msgError) {
                // 消息ack成功，队列成功
                // 业务处理
                sendCallback.onSuccess(new SendResult().setId(id));
            }
        }
        
    }
    
    public Thread getProductThread() {
        return productThread;
    }

    public void setProductThread(Thread productThread) {
        this.productThread = productThread;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isMsgError() {
        return msgError;
    }

    public void setMsgError(boolean msgError) {
        this.msgError = msgError;
    }

    public Thread getCallbackThread() {
        return callbackThread;
    }

    public void setCallbackThread(Thread callbackThread) {
        this.callbackThread = callbackThread;
    }

    public SendCallback getSendCallback() {
        return sendCallback;
    }

    public void setSendCallback(SendCallback sendCallback) {
        this.sendCallback = sendCallback;
    }
    
}