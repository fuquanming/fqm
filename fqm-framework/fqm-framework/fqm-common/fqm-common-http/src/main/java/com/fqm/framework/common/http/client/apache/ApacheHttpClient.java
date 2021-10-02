/*
 * @(#)ApacheHttpClient.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-core
 * 创建日期 : 2021年3月18日
 * 修改历史 : 
 *     1. [2021年3月18日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.http.client.apache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.core.util.StringUtil;
import com.fqm.framework.common.core.util.io.IoUtil;
import com.fqm.framework.common.http.Header;
import com.fqm.framework.common.http.HttpUtil;
import com.fqm.framework.common.http.ProxyInfo;
import com.fqm.framework.common.http.client.HttpClient;
import com.fqm.framework.common.http.file.HttpInputStream;



/**
 * 
 * @version 
 * @author 傅泉明
 */
public class ApacheHttpClient implements HttpClient {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * httpClient
     */
    private CloseableHttpClient client = HttpClientUtil.createHttpClient(30000, 60000);
    
    /**
     * 
     * @param connectTimeout    小于0，返回 null
     * @param readTimeout       小于0，返回 null
     * @return
     */
    public RequestConfig getRequestConfig(int connectTimeout, int readTimeout) {
        ProxyInfo proxyInfo = HttpUtil.getProxy();
        if (proxyInfo != null) {
            Builder builder = RequestConfig.custom().setProxy(new HttpHost(proxyInfo.getIp(), proxyInfo.getPort()));
            if (connectTimeout > 0 && readTimeout > 0) {
                return builder.setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
            } else {
                return builder.build();
            }
        } else if (connectTimeout > 0 && readTimeout > 0) {
            return RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).build();
        }
        return null;
    }

    public String getString(HttpEntity entity, String charset) throws Exception {
        if (entity == null) return "";
        if (charset == null) charset = DEFAULT_CHARSET;
        String str = EntityUtils.toString(entity, charset);
        EntityUtils.consume(entity);
        return str;
    }

    public byte[] getByte(HttpEntity entity) throws Exception {
        if (entity == null) return new byte[0];
        byte[] bytes = EntityUtils.toByteArray(entity);
        EntityUtils.consume(entity);
        return bytes;
    }

    public Object sendDataHttpEntity(HttpUriRequest request, ResultType resultType, Map<String, String> headMap, String charset)
            throws Exception {
        if (headMap != null) {
            for (Map.Entry<String, String> entry : headMap.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        try (CloseableHttpResponse httpResponse = client.execute(request);) {
            HttpEntity entity = httpResponse.getEntity();
            
            if (ResultType.bytes == resultType) {
                return getByte(entity);
            } else if (ResultType.string == resultType) {
                return getString(entity, null);
            } else {
                return null;
            }
        }
    }

    /**
     * 发送get请求
     *
     * @param urlString 网址
     * @return 返回内容
     * @throws Exception 
     */
    public String get(String urlString) throws Exception {
        return (String) sendDataHttpEntity(new HttpGet(urlString), ResultType.string, null, null);
    }

    /**
     * 发送get请求
     *
     * @param urlString 网址
     * @return 返回内容
     * @throws Exception 
     */
    public String get(String urlString, int timeout) throws Exception {
        return (String) sendDataHttpEntity(
                RequestBuilder.get(urlString).setConfig(getRequestConfig(timeout, timeout)).build(),
                ResultType.string,
                null,
                null);
    }

    /**
     * 发送post请求
     *
     * @param urlString      网址
     * @param paramMap       post表单数据
     * @param headMap        请求头
     * @return 返回数据
     * @throws Exception 
     */
    public String post(String urlString, Map<String, Object> paramMap, Map<String, String> headMap) throws Exception {
        return post(urlString, paramMap, headMap, -1);
    }

    /**
     * 发送post请求
     *
     * @param urlString      网址
     * @param paramMap       post表单数据
     * @param headMap        请求头
     * @return 返回数据
     * @throws Exception 
     */
    public String post(String urlString, Map<String, Object> paramMap, Map<String, String> headMap, int timeout) throws Exception {
        RequestBuilder requestBuilder = RequestBuilder.post(urlString);
        /** 是否上传文件 */
        MultipartEntityBuilder multipartEntityBuilder = null;
        
        if (paramMap != null) {
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Collection) {
                    for (Object val : (Collection<?>) value) {
                        requestBuilder.addParameter(key, String.valueOf(val));
                    }
                } else if (value instanceof File) {
                    if (multipartEntityBuilder == null) {
                        multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    }
                    File file = (File) value;
                    FileBody fileBody = new FileBody(file, ContentType.MULTIPART_FORM_DATA, file.getName());
                    multipartEntityBuilder.addPart(key, fileBody);
                } else if (value instanceof HttpInputStream) {
                    if (multipartEntityBuilder == null) {
                        multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    }
                    HttpInputStream is = (HttpInputStream) value;
                    multipartEntityBuilder.addBinaryBody(key, is.getIs(), ContentType.MULTIPART_FORM_DATA, is.getFileName());
                } else {
                    requestBuilder.addParameter(key, String.valueOf(value));
                }
            }
        }

        //        HttpPost request = new HttpPost(urlString);
        //        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
        //        request.setEntity(entity);

        requestBuilder.setCharset(Charset.forName(DEFAULT_CHARSET));
        requestBuilder.setConfig(getRequestConfig(timeout, timeout));
        
        if (multipartEntityBuilder != null) {
            requestBuilder.setEntity(multipartEntityBuilder.build());
        }

        return (String) sendDataHttpEntity(requestBuilder.build(), ResultType.string, headMap, DEFAULT_CHARSET);
    }

    /**
     * 发送post请求<br>
     *
     * @param urlString      网址
     * @param body           body
     * @param headMap        请求头
     * @return 返回数据
     * @throws Exception 
     */
    public String post(String urlString, String body, Map<String, String> headMap) throws Exception {
        return post(urlString, body, headMap, -1, DEFAULT_CHARSET);
    }
    
    /**
     * 发送post请求<br>
     *
     * @param urlString      网址
     * @param body           post表单数据
     * @param headMap        请求头
     * @param charset        请求字符编码
     * @return 返回数据
     * @throws Exception 
     */
    public String post(String urlString, String body, Map<String, String> headMap, int timeout) throws Exception {
        return post(urlString, body, headMap, timeout, DEFAULT_CHARSET);
    }

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
    public String post(String urlString, String body, Map<String, String> headMap, int timeout, String charset) throws Exception {
        if (charset == null) charset = DEFAULT_CHARSET;
        RequestBuilder requestBuilder = RequestBuilder.post(urlString);
        requestBuilder.setCharset(Charset.forName(charset));
        requestBuilder.setConfig(getRequestConfig(timeout, timeout));

        if (body != null && !"".equals(body)) {
            StringEntity reqEntity = new StringEntity(body, charset);
            requestBuilder.setEntity(reqEntity);
        }

        return (String) sendDataHttpEntity(requestBuilder.build(), ResultType.string, headMap, charset);
    }
    
    @Override
    public long downloadFile(String url, OutputStream os) throws Exception {
        return (long) downloadFiles(url, null, null, null, os, -1);
    }
    
    @Override
    public long downloadFile(String url, File destFile) throws Exception {
        return downloadFile(url, null, null, destFile, -1);
    }
    
    /**
     * 下载远程文件
     *
     * @param url            请求的url
     * @param body           请求内容
     * @param destFile       目标文件或目录，当为目录时，取URL中的文件名，取不到使用编码后的URL做为文件名
     * @return 文件大小
     * @throws IOException 
     * @throws  
     */
    public long downloadFile(String url, String body, File destFile) throws Exception {
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
     * @throws IOException 
     * @throws  
     */
    public long downloadFile(String url, String body, Map<String, String> headMap, File destFile) throws Exception {
        return downloadFile(url, body, headMap, destFile, -1);
    }
    
    public long downloadFile(String url, String body, Map<String, String> headMap, File destFile, int timeout) throws Exception {
        return (long) downloadFiles(url, body, headMap, destFile, null, -1);
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
     * @throws IOException 
     * @throws  
     */
    public Object downloadFiles(String url, String body, Map<String, String> headMap, File destFile, OutputStream os, int timeout) throws Exception {
        OutputStream fos = null;
        HttpUriRequest request = null;
        if (StringUtil.isEmpty(body)) {
            request = RequestBuilder.get(url).setConfig(getRequestConfig(timeout, timeout)).build();
        } else {
            StringEntity reqEntity = new StringEntity(body, DEFAULT_CHARSET);
            request = RequestBuilder.post(url).setConfig(getRequestConfig(timeout, timeout))
                    .setEntity(reqEntity)
                    .build();
        }
        
        if (headMap != null) {
            for (Map.Entry<String, String> entry : headMap.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        
        try (CloseableHttpResponse httpResponse = client.execute(request);) {
            
            HttpEntity entity = httpResponse.getEntity();
            
            if (destFile != null) {
                File file = null;
                // 保存的文件名
                if (true == destFile.isDirectory()) {
                    // 从Content-Disposition头中获取文件名
                    String fileName = null;
                    String disposition = null;
                    org.apache.http.Header[] headers = httpResponse.getHeaders(Header.CONTENT_DISPOSITION.getValue());
                    if (headers != null && headers.length > 0) {
                        disposition = headers[0].getValue();
                    }
                    if (StringUtil.isNotBlank(disposition)) {
                        fileName = StringUtil.substringBetween(disposition, "filename=\"", "\"");
                        if (StringUtil.isBlank(fileName)) {
                            fileName = StringUtil.substringAfter(disposition, "filename=");
                        }
                    }
                    
                    if (StringUtil.isBlank(fileName)) {
                        // 从路径中获取文件名
                        fileName = StringUtil.substringAfterLast(url, "/");
                        if (StringUtil.isNotBlank(fileName)) {
                            fileName = URLDecoder.decode(fileName, DEFAULT_CHARSET);// url 获取文件名
                        } else {
                            // 编码后的路径做为文件名
                            fileName = URLEncoder.encode(url, DEFAULT_CHARSET);
                        }
                    } else {
                        fileName = new String(fileName.getBytes("iso-8859-1"), DEFAULT_CHARSET);// 头文件获取的文件名
                    }
                    file = new File(destFile, fileName);
                } else {
                    file = destFile;
                }
                
                fos = new FileOutputStream(file);
            }
            
            if (fos == null) {
                fos = os;
            }
            long size = IoUtil.copyByNIO(entity.getContent(), fos, IoUtil.DEFAULT_BUFFER_SIZE);
            EntityUtils.consume(entity);
            return size;
        } finally {
            if (fos != null) fos.close();
        }
    }
    
    public void destroy() {
        if (client != null) {
            try {
                logger.info("......destroy......");
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    enum ResultType {
        string, bytes;
    }
}
