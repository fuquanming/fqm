package com.fqm.framework.common.file.minio.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.fqm.framework.common.file.FileService;
import com.fqm.framework.common.file.minio.MinioService;
import com.fqm.framework.common.file.minio.config.MinioProperties;

import io.minio.http.Method;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@EnableConfigurationProperties({MinioProperties.class})
public class FileServiceMinioImpl implements FileService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private MinioService minioService;
    @Resource
    private MinioProperties properties;

    @Override
    public boolean deleteFile(String fileId) {
        try {
            return minioService.removeObject(properties.getBucketDefaultName(), fileId);
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
            if (!parentFile.exists()) parentFile.mkdirs();
            minioService.downloadObject(properties.getBucketDefaultName(), fileId, downloadFileName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File downloadFile minio error", e);
        }
        return false;
    }
    
    @Override
    public String getFileUrl(String fileId) {
        try {
            return minioService.getPresignedObjectUrl(properties.getBucketDefaultName(), fileId, 24 * 3600, Method.GET);
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
            minioService.putObject(properties.getBucketDefaultName(), fileName, is);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File upload minio error", e);
        } 
        return null;
    }

}
