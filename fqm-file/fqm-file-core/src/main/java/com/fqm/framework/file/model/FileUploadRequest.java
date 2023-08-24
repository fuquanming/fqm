/*
 * @(#)FileUploadInfo.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2023年7月28日
 * 修改历史 : 
 *     1. [2023年7月28日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.model;

/**
 * 文件上传（分片信息）
 * @version 
 * @author 傅泉明
 */
public class FileUploadRequest {
    
    /** 该文件上传的唯一ID */
    private String requestId;
    /** 该文件内容的MD5值 */
    private String md5;
    /** 该文件总大小 */
    private Long size;
    /** 分片总数 */
    private Integer chunks;
    /** 当前分片，1开始 */
    private Integer chunk;
    
    public String getRequestId() {
        return requestId;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public String getMd5() {
        return md5;
    }
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
    public Integer getChunks() {
        return chunks;
    }
    public void setChunks(Integer chunks) {
        this.chunks = chunks;
    }
    public Integer getChunk() {
        return chunk;
    }
    public void setChunk(Integer chunk) {
        this.chunk = chunk;
    }
}
