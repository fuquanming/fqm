package com.fqm.framework.mq.exception;
/**
 * 
 * 消息异常
 * @version 
 * @author 傅泉明
 */
public class MqException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -9080981023656071927L;

    public MqException(Throwable cause) {
        super(cause);
    }
}
