/*
 * @(#)Headers.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2026年1月9日
 * 修改历史 : 
 *     1. [2026年1月9日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.model;

/**
 * 自定义 HTTP header values.
 * @version 
 * @author 傅泉明
 */
public interface Headers {
    /*
     * 标准 HTTP Headers
     */
    String CACHE_CONTROL = "Cache-Control";
    String CONTENT_DISPOSITION = "Content-Disposition";
    String CONTENT_ENCODING = "Content-Encoding";
    String CONTENT_LENGTH = "Content-Length";
    String CONTENT_RANGE = "Content-Range";
    String CONTENT_MD5 = "Content-MD5";
    String CONTENT_TYPE = "Content-Type";
    String CONTENT_LANGUAGE = "Content-Language";
    String DATE = "Date";
    String ETAG = "ETag";
    String LAST_MODIFIED = "Last-Modified";
    String SERVER = "Server";
    String CONNECTION = "Connection";
}
