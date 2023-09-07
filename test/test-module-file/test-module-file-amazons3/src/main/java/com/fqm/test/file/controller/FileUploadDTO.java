package com.fqm.test.file.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "FileUpload", description = "文件分片上传实体")
public class FileUploadDTO {

    @ApiModelProperty(value = "md5", notes = "webuploader 自带的md5算法值， 与后端生成的不一致")
    private String md5;
    @ApiModelProperty(value = "大小")
    private Long size;
    @ApiModelProperty(value = "文件唯一名 md5.js生成的, 与后端生成的一致")
    private String name;
    @ApiModelProperty(value = "分片总数")
    private Integer chunks;
    @ApiModelProperty(value = "当前分片")
    private Integer chunk;
    @ApiModelProperty(value = "最后更新时间")
    private String lastModifiedDate;
    @ApiModelProperty(value = "类型")
    private String type;
    @ApiModelProperty(value = "后缀")
    private String ext;
    @ApiModelProperty(value = "文件夹id")
    private Long folderId;
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public String getLastModifiedDate() {
        return lastModifiedDate;
    }
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
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
    
}
