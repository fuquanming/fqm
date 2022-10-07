/*
 * @(#)HttpUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年3月16日
 * 修改历史 : 
 *     1. [2021年3月16日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.http;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.http.client.HttpClient;
import com.fqm.framework.common.http.client.apache.ApacheHttpClient;

/**
 * Http 请求
 * 
 * 初始化
 * HttpUtil.init(new ApacheHttpClient());
 * 设置代理
 * HttpUtil.setProxy("127.0.0.1", 8888);
 * 请求
 * HttpUtil.get...post...download
 * 
 * 系统停止时，释放资源
 * HttpUtil.destroy();
 * 
 * @version 
 * @author 傅泉明
 */
public class HttpUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static ThreadLocal<ProxyInfo> proxyLocal = new ThreadLocal<>();

    /** 超时时间，单位毫秒 */
    public static int timeout = 60000;
    
    private static volatile HttpClient httpClient;
    /**
     * 设置代理 ip 和 端口
     * @param ip
     * @param port
     */
    public static void setProxy(String ip, int port) {
        proxyLocal.set(new ProxyInfo().setIp(ip).setPort(port));
    }
    
    /**
     * 获取代理信息 
     * @return
     */
    public static ProxyInfo getProxy() {
        return proxyLocal.get();
    }
    /**
     * 移除代理信息
     */
    public static void removeProxy() {
        proxyLocal.remove();
    }
    
    public static void init(HttpClient client) {
        httpClient = client;
    }
    
    private static class ApacheHttpClientHolder {
        private static HttpClient INSTANCE = new ApacheHttpClient();
    }
    
    public static HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = ApacheHttpClientHolder.INSTANCE;
        }
        return httpClient;
    }

    /**
     * 发送get请求
     *
     * @param urlString 网址
     * @return 返回内容
     */
    public static String get(String urlString) {
        String data = null;
        try {
            data = getHttpClient().get(urlString);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("get error", e);
        } finally {
            removeProxy();
        }
        return data;
    }

    /**
     * 发送post请求
     *
     * @param urlString      网址
     * @param paramMap       post表单数据，String,List<String>;String,File;String HttpInputStream
     * @param headMap        请求头
     * @return 返回数据
     */
    public static String post(String urlString, Map<String, Object> paramMap, Map<String, String> headMap) {
        return post(urlString, paramMap, headMap, -1);
    }

    /**
     * 发送post请求
     *
     * @param urlString      网址
     * @param paramMap       post表单数据
     * @param headMap        请求头
     * @return 返回数据
     */
    public static String post(String urlString, Map<String, Object> paramMap, Map<String, String> headMap, int timeout) {
        String data = null;
        try {
            data = getHttpClient().post(urlString, paramMap, headMap, timeout);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("post表单数据 error", e);
        } finally {
            removeProxy();
        }
        return data;
    }

    /**
     * 发送post请求<br>
     *
     * @param urlString      网址
     * @param body           请求内容
     * @param headMap        请求头
     * @return 返回数据
     */
    public static String post(String urlString, String body, Map<String, String> headMap) {
        return post(urlString, body, headMap, -1);
    }

    /**
     * 发送post请求
     *
     * @param urlString      网址
     * @param body           请求内容
     * @param headMap        请求头
     * @return 返回数据
     */
    public static String post(String urlString, String body, Map<String, String> headMap, int timeout) {
        return post(urlString, body, headMap, timeout, null);
    }
    
    /**
     * 发送post请求
     * @param urlString      网址
     * @param body           请求内容
     * @param headMap        请求头
     * @param timeout        超时时间
     * @param charset        请求字符编码
     * @return
     */
    
    public static String post(String urlString, String body, Map<String, String> headMap, int timeout, String charset) {
        String data = null;
        try {
            data = getHttpClient().post(urlString, body, headMap, timeout, charset);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("post body error", e);
        } finally {
            removeProxy();
        }
        return data;
    }
    
    public static long downloadFile(String url, OutputStream os) {
        long length = 0;
        try {
            length = getHttpClient().downloadFile(url, os);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("downloadFile error", e);
        } finally {
            removeProxy();
        }
        return length; 
    }
    
    /**
     * 下载远程文件
     *
     * @param url            请求的url
     * @param destFile       目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     * @throws  
     */
    public static long downloadFile(String url, File destFile) {
        return downloadFile(url, null, null, destFile, -1);
    }
    
    /**
     * 下载远程文件
     *
     * @param url            请求的url
     * @param body           请求内容
     * @param destFile       目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     * @throws  
     */
    public static long downloadFile(String url, String body, File destFile) {
        return downloadFile(url, body, null, destFile, -1);
    }
    
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
    public static long downloadFile(String url, String body, Map<String, String> headMap, File destFile) {
        return downloadFile(url, body, headMap, destFile, -1);
    }
    
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
    public static long downloadFile(String url, String body, Map<String, String> headMap, File destFile, int timeout) {
        long length = 0;
        try {
            length = getHttpClient().downloadFile(url, body, headMap, destFile, timeout);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("downloadFile error", e);
        } finally {
            removeProxy();
        }
        return length;
    }
    

    public static void destroy() {
        getHttpClient().destroy();
        httpClient = null;
    }

}
