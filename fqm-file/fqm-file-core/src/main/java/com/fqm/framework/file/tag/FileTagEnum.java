/*
 * @(#)FileTagEnum.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2024年2月22日
 * 修改历史 : 
 *     1. [2024年2月22日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.tag;

/**
 * 标签枚举 
 * @version 
 * @author 傅泉明
 */
public enum FileTagEnum {

    /**
     * 过期标签Key
     */
    EXPIRY_TAG_KEY("expiry-key-s3"),
    /**
     * 过期标签Value
     */
    EXPIRY_TAG_VALUE("expiry-value-s3");

    private final String value;

    FileTagEnum(String value) {
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

}
