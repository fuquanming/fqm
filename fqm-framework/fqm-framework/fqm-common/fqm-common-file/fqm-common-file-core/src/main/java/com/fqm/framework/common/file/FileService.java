package com.fqm.framework.common.file;

import java.io.File;
import java.io.InputStream;

/**
 * 文件服务，上传下载
 * @version 
 * @author 傅泉明
 */
public interface FileService {
    
    /**
     * 删除文件 根据 文件标识 来删除一个文件（上传文件时直接将fileId保存在了数据库中）
     * @param fileId    文件标识，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     * @return          
     */
    public boolean deleteFile(String fileId);
    
    /**
     * 文件下载
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @param downloadFileName  保存的文件，全路径
     */
    public boolean downloadFile(String fileId, String downloadFileName);
    
    /**
     * 获取文件访问的URL地址
     * @param fileId            文件标识  如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394_181x161.jpg
     * @return
     */
    default String getFileUrl(String fileId) {
        return null;
    }
    
    /**
     * 上传文件
     * @param file      文件
     * @param fileName  文件名，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg     
     * @return          文件标识，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     */
    public String uploadFile(File file, String fileName);

    /**
     * 上传文件，未关闭流，需要自己关闭流
     * @param is        文件流
     * @param fileName  文件名，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     * @return          文件标识，如：group1/M00/00/00/wKjTgFo7cNGAI8TvAALa-N2N974394.jpg
     */
    public String uploadFile(InputStream is, String fileName);
    
    
}
