package com.fqm.framework.file.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.file.FileMode;
import com.fqm.framework.file.minio.MinioService;

import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Item;

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

    /**
     * 支持目录删除 
     * @see com.fqm.framework.file.template.FileTemplate#deleteFile(java.lang.String)
     */
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
    public boolean deleteFile(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return true;
        }
        try {
            List<String> removeObjects = minioService.removeObjects(bucketDefaultName, fileIds);
            if (removeObjects.isEmpty()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File deleteFiles minio error", e);
        } 
        return false;
    }
    
    @Override
    public boolean deleteDir(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            logger.error("filePath is empty");
            return false;
        }
        try {
            int size = 100;
            List<String> deleteFiles = new ArrayList<>(size);
            do {
                deleteFiles.clear();
                Iterable<Result<Item>> listObjects = minioService.listObjects(bucketDefaultName, filePath, null, true, size);
                for (Result<Item> result : listObjects) {
                    deleteFiles.add(result.get().objectName());
                }
                if (deleteFiles.isEmpty()) {
                    return true;
                }
                boolean flag = deleteFile(deleteFiles);
                if (!flag) {
                    return flag;
                }
            } while (!deleteFiles.isEmpty());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File deleteDir minio error", e);
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
    
    @Override
    public InputStream downloadFile(String fileId) {
        try {
            return minioService.downloadObject(bucketDefaultName, fileId);
        } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException
                | NoSuchAlgorithmException | ServerException | XmlParserException | IOException e) {
            e.printStackTrace();
        }
        return null;
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
        return FileMode.MINIO;
    }
    
    public MinioService getMinioService() {
        return this.minioService;
    }
}
