package com.fqm.framework.mq.callback;

import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData.Confirm;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;

/**
 * 设置确认回调，单线程轮训监听通知
 * 
 * extends WeakReference
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
    private volatile boolean error = false;
    /** 异常信息 */
    private volatile String errorMsg;
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
        error = true;
        errorMsg = "ERROR exchange onFailure";
        LockSupport.unpark(productThread);
        // 同步消息
        if (sendCallback != null) {
            sendCallback.onException(ex);
        }
    }
    @Override
    public void onSuccess(Confirm confirm) {
        // @RabbitPropertiesBeanPostProcessor 参考
        // 设置确认回调 ACK
        callbackThread = Thread.currentThread();
        // 入交换机失败
        if (!confirm.isAck()) {
            error = true;
            errorMsg = "ERROR exchange ack";
        }
        // 等待入队列异常通知，不处理
        if (sendCallback == null) {
            // 同步消息
            LockSupport.unpark(productThread);
        } else {// 异步消息
            if (error) {
                sendCallback.onException(new RuntimeException(errorMsg));
            } else {
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
    
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
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