package com.fqm.framework.common.core.vo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fqm.framework.common.core.exception.ErrorCode;
import com.fqm.framework.common.core.exception.GlobalException;
import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 全局统一返回结果类
 * @version 
 * @author 傅泉明
 */
@ApiModel(value = "全局统一返回结果")
public class Result<T> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "返回码(200:表示成功)", example = "200")
    private Integer code;

    @ApiModelProperty(value = "返回消息", example = "成功")
    private String message;

    @ApiModelProperty(value = "返回数据")
    private T data;
    
    /**
     * 错误明细，内部调试错误
     */
    @ApiModelProperty(value = "错误信息")
    private String detailMessage;

    public Result() {
    }

    protected static <T> Result<T> build(T data) {
        Result<T> r = new Result<T>();
        if (data != null) {
            r.setData(data);
        }
        return r;
    }

    public static <T> Result<T> build(T body, ErrorCode errorCode) {
        Result<T> r = build(body);
        r.setCode(errorCode.getCode());
        r.setMessage(errorCode.getMessage());
        return r;
    }

    public static <T> Result<T> build(Integer code, String message) {
        Result<T> r = build(null);
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
    
    public static <T> Result<T> build(Integer code, String message, String detailMessage) {
        Result<T> r = build(null);
        r.setCode(code);
        r.setMessage(message);
        r.setDetailMessage(detailMessage);
        return r;
    }

    public static <T> Result<T> ok() {
        return Result.ok(null);
    }

    /**
     * 操作成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> ok(T data) {
        return build(data, GlobalErrorCodeConstants.SUCCESS);
    }

    public static <T> Result<T> fail() {
        return Result.fail(null);
    }

    /**
     * 操作失败
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Result<T> fail(T data) {
        return build(data, GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
    }

    public Result<T> message(String msg) {
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code) {
        this.setCode(code);
        return this;
    }

    public boolean isOk() {
        if (this.getCode().intValue() == GlobalErrorCodeConstants.SUCCESS.getCode().intValue()) {
            return true;
        }
        return false;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }
    
    public String getDetailMessage() {
        return detailMessage;
    }

    public Result<T> setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
    
//    @JSONField(serialize = false)
    /**
     * 避免序列化
     * @return
     */
    @JsonIgnore
    public boolean isSuccess() {
        return GlobalErrorCodeConstants.SUCCESS.getCode().equals(code);
    }

//    @JSONField(serialize = false)
    /**
     * 避免序列化
     * @return
     */
    @JsonIgnore
    public boolean isError() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "R{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", detailMessage='" + detailMessage + '\'' +
                '}';
    }
    
    // ========= 和 Exception 异常体系集成 =========

    /**
     * 判断是否有异常。如果有，则抛出 {@link GlobalException} 或 {@link ServiceException} 异常
     */
    public void checkError() throws GlobalException, ServiceException {
        if (isSuccess()) {
            return;
        }
        // 全局异常
        if (GlobalErrorCodeConstants.isMatch(code)) {
            throw new GlobalException(code, message).setDetailMessage(detailMessage);
        }
        // 业务异常
        throw new ServiceException(code, message).setDetailMessage(detailMessage);
    }
    
    public static <T> Result<T> error(ServiceException serviceException) {
        return build(serviceException.getCode(), serviceException.getMessage(),
                serviceException.getDetailMessage());
    }

    public static <T> Result<T> error(GlobalException globalException) {
        return build(globalException.getCode(), globalException.getMessage(),
                globalException.getDetailMessage());
    }
}
