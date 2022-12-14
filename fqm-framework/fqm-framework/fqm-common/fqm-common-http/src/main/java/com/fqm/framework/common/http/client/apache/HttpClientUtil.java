/*
 * @(#)HttpClientUtil.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年2月4日
 * 修改历史 : 
 *     1. [2021年2月4日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.http.client.apache;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class HttpClientUtil {

    public static SSLContext getSSLContext() {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new TrustAllManager();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sc;
    }

    static class TrustAllManager implements TrustManager, X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            return;
        }
    }

    /** 10秒连接，200秒读 */
    public static HttpClient createHttpClient() {
        return createHttpClient(10000, 200000);
    }

    public static CloseableHttpClient createHttpClient(int connectionTimeout, int readTimeout) {
        //      if (connectionTimeout <= 0) connectionTimeout = 10000;
        //      if (readTimeout <= 0) readTimeout = 60000;

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(HttpClientUtil.getSSLContext(),
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();

        //        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry, null, null, null, 60, TimeUnit.SECONDS);// 60秒长连接
        //设置连接参数
        manager.setMaxTotal(800); // 最大连接数
        manager.setDefaultMaxPerRoute(500); // 路由最大连接数

        Builder builder = RequestConfig.custom().setConnectionRequestTimeout(1000)//从连接池中获取连接的超时时间
                .setConnectTimeout(connectionTimeout)//连接上服务器的超时时间
                .setSocketTimeout(readTimeout)//返回数据的超时时间
        ;
//        if (proxyIp != null && proxyPort > 0) {
//            builder.setProxy(new HttpHost(proxyIp, proxyPort));
//        }

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))// 重试次数3次，并开启
                .setDefaultRequestConfig(builder.build()).build();
        return client;
    }
    
    public static void closeHttpClient(CloseableHttpClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
