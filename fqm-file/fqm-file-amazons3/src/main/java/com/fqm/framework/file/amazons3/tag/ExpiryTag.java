/*
 * @(#)ExpiryTag.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-amazons3
 * 创建日期 : 2024年2月18日
 * 修改历史 : 
 *     1. [2024年2月18日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.amazons3.tag;

import com.amazonaws.services.s3.model.Tag;

/**
 * 对象过期标签
 * @version 
 * @author 傅泉明
 */
public class ExpiryTag {

    private static final String EXPIRY_TAG_KEY = "expiry-key-s3";
    private static final String EXPIRY_TAG_VALUE = "expiry-value-s3";
    
    private ExpiryTag() {
    }
    
    public static Tag build() {
        return new Tag(EXPIRY_TAG_KEY, EXPIRY_TAG_VALUE);
    }
    
}
