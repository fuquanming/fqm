/*
 * @(#)FileUploadStatus.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2023年7月28日
 * 修改历史 : 
 *     1. [2023年7月28日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.enums;

/**
 * 文件上传状态
 * @version 
 * @author 傅泉明
 */
public enum FileUploadStatus {

    /**
     * 成功
     */
    SUCCESS("success"),
    
    /**
     * 失败
     */
    FAIL("fail"),
    
    /**
     * 上传中
     */
    UPLOADING("uploading");
    
    private final String value;

    FileUploadStatus(String value) {
        this.value = value;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public String getValue(){
        return this.value;
    }

    @Override
    public String toString() {
        return getValue();
    }
    
}
