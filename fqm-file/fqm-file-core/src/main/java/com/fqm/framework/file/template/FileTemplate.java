package com.fqm.framework.file.template;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.fqm.framework.file.FileMode;
import com.fqm.framework.file.model.FileUploadRequest;
import com.fqm.framework.file.model.FileUploadResponse;
import com.fqm.framework.file.tag.FileTag;
/**
 * 文件模板
 * 
 * @version 
 * @author 傅泉明
 */
public interface FileTemplate {
    /**
     * 获取文件存储的方式 
     * @return
     */
    public FileMode getFileMode();
    
    /**
     * 删除文件 根据 文件标识 来删除一个文件（上传文件时直接将fileId保存在了数据库中）
     * @param fileId    文件标识，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     * @return          
     */
    public boolean deleteFile(String fileId);
    
    /**
     * 删除文件 根据 文件标识 来删除多个文件（上传文件时直接将fileId保存在了数据库中）
     * @param fileIds    文件标识，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     * @return          
     */
    public boolean deleteFile(List<String> fileIds);
    
    /**
     * 删除文件目录 根据 文件夹路径 来删除文件目录（上传文件时直接将fileId保存在了数据库中）
     * @param filePath    文件标识，如：group1/M00/00/00
     * @return          
     */
    default boolean deleteDir(String filePath) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->删除文件目录");
    }
    
    /**
     * 文件下载
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @param downloadFileName  保存的文件，全路径
     * @return
     */
    public boolean downloadFile(String fileId, String downloadFileName);
    
    /**
     * 文件下载
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @return
     */
    public InputStream downloadFile(String fileId);
    
    /**
     * 获取文件访问的URL地址
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @return
     */
    default String getFileUrl(String fileId) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->获取文件访问的URL地址");
    }
    
    /**
     * 获取有时效文件访问的URL地址
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @param expireSecond      时效，单位秒
     * @return
     */
    default String getFileUrlExpires(String fileId, Integer expireSecond) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->获取有时效文件访问的URL地址");
    }
    
    /**
     * 上传文件
     * @param file      文件
     * @param fileName  上传后的文件名，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg     
     * @return          文件标识，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     */
    public String uploadFile(File file, String fileName);

    /**
     * 上传文件，未关闭流，需要自己关闭流
     * @param is        文件流
     * @param fileName  上传后的文件名，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     * @return          文件标识，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     */
    public String uploadFile(InputStream is, String fileName);
    
    /**
     * 上传文件(分片上传)
     * @param fileUploadRequest 分片上传信息
     * @param file              文件
     * @param fileName          上传后的文件名，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg     
     * @return                  分片上传状态
     */
    default FileUploadResponse uploadFile(FileUploadRequest fileUploadRequest, File file, String fileName) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->上传文件(分片上传)");
    }
    
    /**
     * 上传文件(分片上传)
     * @param fileUploadRequest 分片上传信息
     * @param is                文件流
     * @param fileName          上传后的文件名，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg     
     * @return                  分片上传状态
     */
    default FileUploadResponse uploadFile(FileUploadRequest fileUploadRequest, InputStream is, String fileName) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->上传文件(分片上传)");
    }
    
    /**
     * 判断是否是分片上传
     * @param fileUploadRequest
     * @return
     */
    default boolean isFileChunkUpload(FileUploadRequest fileUploadRequest) {
        return (null != fileUploadRequest && null != fileUploadRequest.getChunk() && null != fileUploadRequest.getChunks());
    }
    
    /*** 文件标签 ***/
    /**
     * 获取文件标签
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @return 
     */
    default List<FileTag> getFileTag(String fileId) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->获取文件标签");
    }
    
    /**
     * 设置文件标签（文件的标签会被该集合替换）
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @param tagSet            标签集合
     */
    default void setFileTag(String fileId, List<FileTag> tagSet) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->设置文件标签");
    }
    
    /**
     * 删除文件所有标签
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @param objectName    文件名
     */
    default void deleteFileTag(String fileId) {
        throw new com.fqm.framework.common.core.exception.ServiceException(12, getFileMode() + ":未实现->删除文件所有标签");
    }
}
