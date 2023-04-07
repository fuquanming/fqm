package com.fqm.framework.file.config;

/**
 * AmazonS3 配置类
 * 
 * @version 
 * @author 傅泉明
 */
public class AmazonS3Properties {

    /**
     * 是否开启，默认为 true 关闭
     */
    private Boolean enabled = true;
    /**
     * endPoint是一个URL，域名，IPv4或者IPv6地址
     */
    private String endpoint;

    /**
     * accessKey类似于用户ID，用于唯一标识你的账户
     */
    private String accessKey;

    /**
     * secretKey是你账户的密码
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String bucketName = "config";
    
    /**
     * 区域
     */
    private String region;
    
    /**
     * 代理访问文件服务路径，如：nginx，代理访问minio
     */
    private String accessUrl;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnable(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }
    
    
}
