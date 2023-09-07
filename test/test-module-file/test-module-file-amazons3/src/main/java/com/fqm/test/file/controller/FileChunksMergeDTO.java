package com.fqm.test.file.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 分片合并DTO
 */
@ApiModel(value = "FileChunksMerge", description = "文件合并实体")
public class FileChunksMergeDTO {
    @ApiModelProperty(value = "文件唯一名 md5.js 生成的, 与后端生成的一致")
    private String name;
    @ApiModelProperty(value = "原始文件名")
    private String submittedFileName;

    @ApiModelProperty(value = "md5", notes = "webuploader 自带的md5算法值， 与后端生成的不一致")
    private String md5;

    @ApiModelProperty(value = "分片总数")
    private Integer chunks;
    @ApiModelProperty(value = "后缀")
    private String ext;
    @ApiModelProperty(value = "文件夹id")
    private Long folderId;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSubmittedFileName() {
        return submittedFileName;
    }
    public void setSubmittedFileName(String submittedFileName) {
        this.submittedFileName = submittedFileName;
    }
    public String getMd5() {
        return md5;
    }
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    public Integer getChunks() {
        return chunks;
    }
    public void setChunks(Integer chunks) {
        this.chunks = chunks;
    }
    public String getExt() {
        return ext;
    }
    public void setExt(String ext) {
        this.ext = ext;
    }
    public Long getFolderId() {
        return folderId;
    }
    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
    public String getContextType() {
        return contextType;
    }
    public void setContextType(String contextType) {
        this.contextType = contextType;
    }
    @ApiModelProperty(value = "大小")
    private Long size;
    @ApiModelProperty(value = "类型")
    private String contextType;
}
