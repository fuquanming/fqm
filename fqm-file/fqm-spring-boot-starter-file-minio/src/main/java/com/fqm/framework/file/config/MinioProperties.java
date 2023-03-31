package com.fqm.framework.file.config;

/**
 * MinIO 配置类
 * @version 
 * @author 傅泉明
 */
public class MinioProperties {

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
    private String bucketDefaultName = "config";

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

    public String getBucketDefaultName() {
        return bucketDefaultName;
    }

    public void setBucketDefaultName(String bucketDefaultName) {
        this.bucketDefaultName = bucketDefaultName;
    }
    
}
