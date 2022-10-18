package com.fqm.framework.common.core.exception;

/**
 * 业务逻辑异常 Exception
 * 
 * @version 
 * @author 傅泉明
 */
@SuppressWarnings("serial")
public class ServiceException extends RuntimeException {

    /**
     * 业务错误码
     *
     * @see ServiceErrorCodeRange
     */
    private final Integer code;
    /**
     * 错误提示
     */
    private final String message;
    /**
     * 错误明细，内部调试错误
     *
     * 和 {@link R#getDetailMessage()} 一致的设计
     */
    private final String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
        this.code = null;
        this.message = null;
        this.detailMessage = null;
    }

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detailMessage = null;
    }

    public ServiceException(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.detailMessage = null;
    }
    
    public ServiceException(Integer code, String message, String detailMessage) {
        this.code = code;
        this.message = message;
        this.detailMessage = detailMessage;
    }

    public Integer getCode() {
        return code;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 移除父类同步方法
     * @see java.lang.Throwable#fillInStackTrace()
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
