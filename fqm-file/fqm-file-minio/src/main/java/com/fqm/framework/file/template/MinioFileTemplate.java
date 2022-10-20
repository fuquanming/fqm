package com.fqm.framework.file.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.file.FileMode;
import com.fqm.framework.file.minio.MinioService;

import io.minio.http.Method;

/**
 * Minio文件存储
 * 
 * @version 
 * @author 傅泉明
 */
public class MinioFileTemplate implements FileTemplate {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private MinioService minioService;
    private String bucketDefaultName;
    
    public MinioFileTemplate(MinioService minioService, String bucketDefaultName) {
        this.minioService = minioService;
        this.bucketDefaultName = bucketDefaultName;
    }

    @Override
    public boolean deleteFile(String fileId) {
        try {
            return minioService.removeObject(bucketDefaultName, fileId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File deleteFile minio error", e);
        } 
        return false;
    }
    
    @Override
    public boolean downloadFile(String fileId, String downloadFileName) {
        try {
            File file = new File(downloadFileName);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            minioService.downloadObject(bucketDefaultName, fileId, downloadFileName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File downloadFile minio error", e);
        }
        return false;
    }
    
    /**
     * 1、Buckets-> 选择一个Bucket -> 点击Manage
     * 2、选择"AccessRules"，添加新的Access Rule，prefix=*，Access=readonly 
     * @see com.fqm.framework.file.template.FileTemplate#getFileUrl(java.lang.String)
     *
     */
    @Override
    public String getFileUrl(String fileId) {
        try {
            return minioService.getObjectUrl(bucketDefaultName, fileId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public String getFileUrlExpires(String fileId, Integer expireSecond) {
        try {
            return minioService.getPresignedObjectUrl(bucketDefaultName, fileId, 24 * 3600, Method.GET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String uploadFile(File file, String fileName) {
        try (InputStream is = new FileInputStream(file)) {
            return uploadFile(is, fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("File upload minio FileNotFoundException", e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("File upload minio IOException", e);
        } 
        return null;
    }

    @Override
    public String uploadFile(InputStream is, String fileName) {
        try {
            minioService.putObject(bucketDefaultName, fileName, is);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File upload minio error", e);
        } 
        return null;
    }

    @Override
    public FileMode getFileMode() {
        return FileMode.minio;
    }
}
