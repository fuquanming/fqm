/*
 * @(#)LockException.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-lock-core
 * 创建日期 : 2022年10月20日
 * 修改历史 : 
 *     1. [2022年10月20日]创建文件 by 傅泉明
 */
package com.fqm.framework.locks.exception;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class LockException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -4579742009183015740L;

    public LockException(Throwable cause) {
        super(cause);
    }
    
    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
