package com.fqm.framework.file.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fqm.framework.file.FileMode;
import com.fqm.framework.file.amazons3.AmazonS3Service;

/**
 * AmazonS3 文件存储
 * 
 * @version 
 * @author 傅泉明
 */
public class AmazonS3FileTemplate implements FileTemplate {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private AmazonS3Service service;
    private String bucketName;
    private String accessUrl;
    
    /**
     * 
     * @param service   
     * @param accessUrl 代理访问文件服务路径
     * 如：nginx代理
     *   location /s3/ {
             #proxy_set_header Host $http_host; 开启则限时访问路径无法访问，出现 SignatureDoesNotMatch
             proxy_set_header Host 192.168.1.100:11000; # 如果有限时访问路径只能指定Host为Minio访问的ip和端口
             proxy_set_header X-Real-IP $remote_addr;
             proxy_set_header REMOTE-HOST $remote_addr;
             proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
             proxy_pass http://xxx/桶/;
         }
     * 
     */
    public AmazonS3FileTemplate(AmazonS3Service service, String accessUrl) {
        this.service = service;
        this.bucketName = service.getBucketName();
        this.accessUrl = accessUrl;
    }
    
    @Override
    public FileMode getFileMode() {
        return FileMode.AMAZONS3;
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
                ListObjectsV2Result listObjects = service.listObjects(bucketName, filePath, null, size);
                for (S3ObjectSummary object : listObjects.getObjectSummaries()) {
                    deleteFiles.add(object.getKey());
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
            logger.error("File deleteDir error", e);
        } 
        return false;
    }

    @Override
    public boolean deleteFile(String fileId) {
        try {
            service.deleteObject(bucketName, fileId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File deleteFile error", e);
        } 
        return false;
    }

    @Override
    public boolean deleteFile(List<String> fileIds) {
        try {
            service.deleteObjects(bucketName, fileIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File deleteFiles error", e);
        } 
        return false;
    }
    
    @Override
    public InputStream downloadFile(String fileId) {
        return service.getObject(bucketName, fileId);
    }

    @Override
    public boolean downloadFile(String fileId, String downloadFileName) {
        try {
            File file = new File(downloadFileName);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            service.getObject(bucketName, fileId, file);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("File downloadFile error", e);
        }
        return false;
    }

    @Override
    public String getFileUrl(String fileId) {
        if (StringUtils.isNotBlank(accessUrl)) {
            StringBuilder data = new StringBuilder(accessUrl);
            String character = "/";
            if (!accessUrl.endsWith(character)) {
                data.append(character);
            }
            data.append(fileId);
            return data.toString();
        }
        return service.getObjectUrl(bucketName, fileId);
    }
    
    @Override
    public String getFileUrlExpires(String fileId, Integer expireSecond) {
        try {
            String url = service.getPresignedObjectUrl(bucketName, fileId, expireSecond);
            if (StringUtils.isNotBlank(accessUrl)) {
                // 替换为代理url地址
                url = url.replace(service.getEndpoint() + "/" + bucketName + "/", accessUrl);
                return url;
            }
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public String uploadFile(File file, String fileName) {
        service.putObject(bucketName, fileName, file);
        return fileName;
    }

    @Override
    public String uploadFile(InputStream is, String fileName) {
        try {
            service.putObject(bucketName, fileName, is);
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
