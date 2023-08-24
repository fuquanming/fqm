package com.fqm.framework.file.amazons3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants;

/**
 * AmazonS3 协议的文件服务
 * 
 * @version 
 * @author 傅泉明
 */
public class AmazonS3Service {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    private AmazonS3 client;

    private String endpoint;
    private String bucketName;

    /**
     * 构建客户端
     * @param endpoint      endPoint是一个URL，域名，IPv4或者IPv6地址
     * @param accessKey     类似于用户ID，用于唯一标识你的账户
     * @param secretKey     账户的密码
     * @param bucketName    默认存储桶名称
     * @param region        区域
     */
    public AmazonS3Service(String endpoint, String accessKey, String secretKey, String bucketName, String region) {
        this.bucketName = bucketName;
        this.endpoint = endpoint;
        try {
            AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            AmazonS3ClientBuilder build = AmazonS3Client.builder().withEndpointConfiguration(endpointConfig).withCredentials(credentialsProvider)
                    .disableChunkedEncoding();
            this.client = build.build();
            if (!bucketExists(bucketName)) {
                throw new ServiceException(404, "桶不存在:" + bucketName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("初始化AmazonS3失败", e);
            throw new ServiceException(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 检查桶是否存在
     * @param bucketName    桶名字
     * @return
     */
    public boolean bucketExists(String bucketName) {
        return client.doesBucketExistV2(bucketName);
    }

    /**
     * 上传文件
     * @param bucketName    桶
     * @param objectName    文件名
     * @param file          要上传的文件
     * @return
     */
    public boolean putObject(String bucketName, String objectName, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file);
        // 设置上传对象的 Acl 为公共读
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        client.putObject(putObjectRequest);
        return true;
    }
    
    /**
     * 上传文件
     * @param bucketName    桶
     * @param objectName    文件名
     * @param inputStream   上传的文件流
     * @return
     * @throws IOException 
     */
    public boolean putObject(String bucketName, String objectName, InputStream inputStream) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(inputStream.available());
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream, metadata);
        // 设置上传对象的 Acl 为公共读
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        client.putObject(putObjectRequest);
        return true;
    }

    /**
     * 下载文件
     * @param bucketName        桶名字
     * @param objectName        文件名称：文件路径
     * @param destinationFile   本地保存的文件
     */
    public void getObject(String bucketName, String objectName, File destinationFile) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectName);
        client.getObject(getObjectRequest, destinationFile);
    }

    /**
     * 获取文件流
     * @param bucketName    桶 
     * @param objectName    文件名
     * @return
     */
    public InputStream getObject(String bucketName, String objectName) {
        S3Object object = client.getObject(bucketName, objectName);
        return object.getObjectContent();
    }

    /**
     * 删除文件
     * @param bucketName    桶
     * @param objectName    文件名        
     */
    public void deleteObject(String bucketName, String objectName) {
        client.deleteObject(bucketName, objectName);
    }
    
    /**
     * 删除文件 
     * @param bucketName    桶
     * @param objectNames   文件名列表
     */
    public void deleteObjects(String bucketName, List<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }
        DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName);
        List<KeyVersion> keys = new ArrayList<>(objectNames.size());
        objectNames.forEach(objectName -> keys.add(new KeyVersion(objectName)));
        request.setKeys(keys);
        client.deleteObjects(request);
    }
    
    /**
     * 查询文件列表
     * @param bucketName    桶
     * @param prefix        在该文件下查询
     * @param after         在该文件名后查询
     * @param maxKeys       最大结果
     * @return
     */
    public ListObjectsV2Result listObjects(String bucketName, String prefix, String after, int maxKeys) {
        ListObjectsV2Request request = new ListObjectsV2Request();
        request.setBucketName(bucketName);
        if (prefix != null && prefix.length() > 0) {
            request.setPrefix(prefix);
        }
        if (after != null && after.length() > 0) {
            request.setStartAfter(after);
        }
        if (maxKeys > 0) {
            request.setMaxKeys(maxKeys);
        }
        return client.listObjectsV2(request);
    }
    
    /**
     * 获取文件访问地址
     * @param bucketName    桶
     * @param objectName    文件名称
     * @return
     */
    public String getObjectUrl(String bucketName, String objectName) {
        return client.getUrl(bucketName, objectName).toString();
    }

    /**
     * 获得有时限的文件访问地址
     * @param bucketName    桶
     * @param objectName    文件名称
     * @param expireSecond  过期时间单位秒
     * @return
     */
    public String getPresignedObjectUrl(String bucketName, String objectName, Integer expireSecond) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectName)
                .withMethod(HttpMethod.GET).withExpiration(new Date(System.currentTimeMillis() + 1000L * expireSecond));
        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    public String getBucketName() {
        return bucketName;
    }
    public String getEndpoint() {
        return endpoint;
    }
    
    public AmazonS3 getClient() {
        return client;
    }

}
