/*
 * @(#)HttpClient.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年3月19日
 * 修改历史 : 
 *     1. [2021年3月19日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.http.client;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

/**
 * Http 请求
 * @version 
 * @author 傅泉明
 */
public interface HttpClient {
    
    /** 默认字符集 */
    public static final String DEFAULT_CHARSET = "UTF-8";
    /**
     * 发送get请求
     *
     * @param urlString 网址
     * @return 返回内容
     */
    public String get(String urlString) throws Exception;

    /**
     * 发送post请求
     *
     * @param urlString      网址
     * @param paramMap       post表单数据
     * @param headMap        请求头
     * @return 返回数据
     */
    public String post(String urlString, Map<String, Object> paramMap, Map<String, String> headMap) throws Exception;

    /**
     * 发送post请求
     *
     * @param urlString      网址
     * @param paramMap       post表单数据
     * @param headMap        请求头
     * @return 返回数据
     */
    public String post(String urlString, Map<String, Object> paramMap, Map<String, String> headMap, int timeout) throws Exception;

    /**
     * 发送post请求<br>
     *
     * @param urlString      网址
     * @param body           请求内容
     * @param headMap        请求头
     * @return 返回数据
     */
    public String post(String urlString, String body, Map<String, String> headMap) throws Exception;

    /**
     * 发送post请求<br>
     *
     * @param urlString      网址
     * @param body           请求内容
     * @param headMap        请求头
     * @return 返回数据
     */
    public String post(String urlString, String body, Map<String, String> headMap, int timeout) throws Exception;
    
    /**
    * 发送post请求<br>
    * 
    * @param urlString      网址
    * @param body           请求内容
    * @param headMap        请求头
    * @param timeout        超时时间
    * @param charset        请求字符编码
    * @return 返回数据
    */
    public String post(String urlString, String body, Map<String, String> headMap, int timeout, String charset) throws Exception;
    
    /**
     * 下载远程文件
     * 
     * @param url           请求的url
     * @param os            输出的流
     * @throws Exception
     */
    public long downloadFile(String url, OutputStream os) throws Exception;
    
    /**
     * 下载远程文件
     *
     * @param url            请求的url
     * @param destFile       目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     * @throws  
     */
    public long downloadFile(String url, File destFile) throws Exception;
    
    /**
     * 下载远程文件
     *
     * @param url            请求的url
     * @param body           请求内容
     * @param destFile       目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     * @throws  
     */
    public long downloadFile(String url, String body, File destFile) throws Exception;
    
    
    /**
     * 下载远程文件
     *
     * @param url            请求的url
     * @param body           请求内容
     * @param headMap        请求头
     * @param destFile       目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     * @throws  
     */
    public long downloadFile(String url, String body, Map<String, String> headMap, File destFile) throws Exception;
    
    /**
     * 下载远程文件
     *
     * @param url            请求的url
     * @param body           请求内容
     * @param headMap        请求头
     * @param destFile       目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @param timeout        超时，单位毫秒，-1表示默认超时
     * @return 文件大小
     * @throws  
     */
    public long downloadFile(String url, String body, Map<String, String> headMap, File destFile, int timeout) throws Exception;
    

    default void destroy() {
    }
    
}
