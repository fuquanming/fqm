/*
 * @(#)FileObjectMetadata.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2026年1月8日
 * 修改历史 : 
 *     1. [2026年1月8日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.model;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * 文件对象用户提供的元数据，接收的标准 HTTP 标头
 * @version 
 * @author 傅泉明
 */
public class FileObjectMetadata {
    /**
     * 所有其他（非用户自定义）标头，例如 Content-Length、Content-Type、
     */
    private Map<String, Object> metadata = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Date getLastModified() {
        return new Date(((Date)metadata.get(Headers.LAST_MODIFIED)).getTime());
    }

    public void setLastModified(Date lastModified) {
        metadata.put(Headers.LAST_MODIFIED, lastModified);
    }
    
    public long getContentLength() {
        Long contentLength = (Long)metadata.get(Headers.CONTENT_LENGTH);

        if (contentLength == null) return 0;
        return contentLength.longValue();
    }

    /**
     * 返回存储在 S3 中的整个对象的物理长度
     * 例如，这在执行范围获取操作时非常有用
     */
    public long getInstanceLength() {
        // See Content-Range in
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
        String contentRange = (String)metadata.get(Headers.CONTENT_RANGE);
        if (contentRange != null) {
            int pos = contentRange.lastIndexOf("/");
            if (pos >= 0)
                return Long.parseLong(contentRange.substring(pos+1));
        }
        return getContentLength();
    }

    public FileObjectMetadata setContentLength(long contentLength) {
        metadata.put(Headers.CONTENT_LENGTH, contentLength);
        return this;
    }

    public String getContentType() {
        return (String)metadata.get(Headers.CONTENT_TYPE);
    }

    public FileObjectMetadata setContentType(String contentType) {
        metadata.put(Headers.CONTENT_TYPE, contentType);
        return this;
    }

    public String getContentLanguage() {
        return (String)metadata.get(Headers.CONTENT_LANGUAGE);
    }

    public FileObjectMetadata setContentLanguage(String contentLanguage) {
        metadata.put(Headers.CONTENT_LANGUAGE, contentLanguage);
        return this;
    }

    public String getContentEncoding() {
        return (String)metadata.get(Headers.CONTENT_ENCODING);
    }

    public FileObjectMetadata setContentEncoding(String encoding) {
        metadata.put(Headers.CONTENT_ENCODING, encoding);
        return this;
    }

    public String getCacheControl() {
        return (String)metadata.get(Headers.CACHE_CONTROL);
    }

    public FileObjectMetadata setCacheControl(String cacheControl) {
        metadata.put(Headers.CACHE_CONTROL, cacheControl);
        return this;
    }

    public FileObjectMetadata setContentMD5(String md5Base64) {
        if(md5Base64 == null){
            metadata.remove(Headers.CONTENT_MD5);
        }else{
            metadata.put(Headers.CONTENT_MD5, md5Base64);
        }
        return this;
    }

    public String getContentMD5() {
        return (String)metadata.get(Headers.CONTENT_MD5);
    }

    public FileObjectMetadata setContentDisposition(String disposition) {
        metadata.put(Headers.CONTENT_DISPOSITION, disposition);
        return this;
    }

    public String getContentDisposition() {
        return (String)metadata.get(Headers.CONTENT_DISPOSITION);
    }

    public String getETag() {
        return (String)metadata.get(Headers.ETAG);
    }
}
