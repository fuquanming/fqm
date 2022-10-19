package com.fqm.framework.common.core.exception;

/**
 * 业务逻辑异常 Exception
 * 
 * @version 
 * @author 傅泉明
 */
public class ServiceException extends GlobalException {

    /**
     * 
     */
    private static final long serialVersionUID = -545134474234880618L;

    /**
     * 空构造方法，避免反序列化问题
     */
    public ServiceException() {
    }

    public ServiceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ServiceException(Integer code, String message) {
        super(code, message);
    }
    
    public ServiceException(Integer code, String message, String detailMessage) {
        super(code, message, detailMessage);
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
