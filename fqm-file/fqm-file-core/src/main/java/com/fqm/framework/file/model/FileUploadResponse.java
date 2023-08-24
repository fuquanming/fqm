/*
 * @(#)FileChunkInfo.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-file-core
 * 创建日期 : 2023年7月28日
 * 修改历史 : 
 *     1. [2023年7月28日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.model;

import com.fqm.framework.file.enums.FileUploadStatus;

/**
 * 文件上传（分片信息返回）
 * @version 
 * @author 傅泉明
 */
public class FileUploadResponse extends FileUploadRequest {
    
    /** 生成对应的文件上传ID */
    private String uploadId;
    /** 分片上传状态：true 成功：false 失败 */
    private boolean chunkUploadStatus;
    /** 当前分片大小 */
    private Integer chunkSize;
    /** 文件上传状态 */
    private FileUploadStatus uploadStatus;
    /** 消息 */
    private String msg;
    /** 完成上传后返回的文件标识 */
    private String fileId;
    
    public String getUploadId() {
        return uploadId;
    }
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
    public boolean isChunkUploadStatus() {
        return chunkUploadStatus;
    }
    public void setChunkUploadStatus(boolean chunkUploadStatus) {
        this.chunkUploadStatus = chunkUploadStatus;
    }
    public Integer getChunkSize() {
        return chunkSize;
    }
    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }
    public FileUploadStatus getUploadStatus() {
        return uploadStatus;
    }
    public void setUploadStatus(FileUploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
}
