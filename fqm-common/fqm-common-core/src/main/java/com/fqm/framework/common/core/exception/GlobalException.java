package com.fqm.framework.common.core.exception;


/**
 * 全局异常 Exception
 * 
 * @version 
 * @author 傅泉明
 */
@SuppressWarnings("serial")
public class GlobalException extends RuntimeException {

    /**
     * 全局错误码
     *
     * @see GlobalErrorCodeConstants
     */
    private final Integer code;
    /**
     * 错误提示
     */
    private final String message;
    /**
     * 错误明细，内部调试错误
     *
     * 和 {@link CommonResult#getDetailMessage()} 一致的设计
     */
    private final String detailMessage;

    /**
     * 空构造方法，避免反序列化问题
     */
    public GlobalException() {
        this.code = null;
        this.message = null;
        this.detailMessage = null;
    }
    
    public GlobalException(Throwable cause) {
        super(cause);
        this.code = null;
        this.message = null;
        this.detailMessage = null;
    }

    public GlobalException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detailMessage = null;
    }

    public GlobalException(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.detailMessage = null;
    }
    
    public GlobalException(Integer code, String message, String detailMessage) {
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
}
