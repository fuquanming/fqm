package com.fqm.framework.file.amazons3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.validation.constraints.NotNull;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.DeleteObjectTaggingRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants;
import com.fqm.framework.file.amazons3.filter.UploadFilter;
import com.fqm.framework.file.model.FileObjectMetadata;

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
    
    /** 上传过滤器，不用aop拦截、事件发布 */
    private List<UploadFilter> uploadFilters = new ArrayList<>();

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
            
            // 创建信任所有证书的 TrustManager
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            // 创建 SSLContext 并禁用证书校验
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // 跳过主机名验证
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true ); 
            // 配置 ClientConfiguration 来跳过 SSL 验证
            ClientConfiguration clientConfig = new ClientConfiguration();
            // 设置自定义的 SSLContext;
            clientConfig.getApacheHttpClientConfig().withSslSocketFactory(sslsf);
            
            AmazonS3ClientBuilder build = AmazonS3Client.builder().withEndpointConfiguration(endpointConfig).withCredentials(credentialsProvider)
                    // 配置不校验ssl
                    .withClientConfiguration(clientConfig)
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
     * 检查对象是否存在
     * @param bucketName    桶
     * @param objectName    对象名称
     * @return
     */
    public boolean objectExists(String bucketName, String objectName) {
        return client.doesObjectExist(bucketName, objectName);
    }

    /**
     * 上传文件
     * @param bucketName    桶
     * @param objectName    文件名
     * @param file          要上传的文件
     * @return
     * @throws IOException 
     */
    public boolean putObject(String bucketName, String objectName, File file) throws IOException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file);
        // 设置上传对象的 Acl 为公共读
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        return putObject(putObjectRequest);
    }
    
    /**
     * 上传文件
     * @param bucketName    桶
     * @param objectName    文件名
     * @param file          要上传的文件
     * @return
     * @throws IOException 
     */
    public boolean putObject(String bucketName, String objectName, File file, FileObjectMetadata fileObjectMetadata) throws IOException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file);
        putObjectRequest.setMetadata(buildObjectMetadata(fileObjectMetadata));
        // 设置上传对象的 Acl 为公共读
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        return putObject(putObjectRequest);
    }
    
    /**
     * 上传文件
     * @param bucketName    桶
     * @param objectName    文件名
     * @param inputStream   上传的文件流
     * @return
     * @throws IOException 
     */
    public boolean putObject(String bucketName, String objectName, InputStream inputStream, FileObjectMetadata fileObjectMetadata) throws IOException {
        ObjectMetadata metadata = buildObjectMetadata(fileObjectMetadata);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream, metadata);
        // 设置上传对象的 Acl 为公共读
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        return putObject(putObjectRequest);
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
        return putObject(putObjectRequest);
    }
    
    /**
     * 上传文件
     * @param request   上传文件的对象
     * @return
     * @throws IOException
     */
    public boolean putObject(PutObjectRequest request) throws IOException {
        beforeUploadFilter(request);
        client.putObject(request);
        afterUploadFilter(request);
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
    
    /***------ 分片上传API 开始 ------***/
    // initiateMultipartUpload -> uploadPart -> completeMultipartUpload
    
    /**
     * 初始化分片上传请求
     * @param bucketName    桶
     * @param objectName    文件名称
     * @return
     * @throws IOException 
     */
    public InitiateMultipartUploadResult initiateMultipartUpload(String bucketName, String objectName, @NotNull FileObjectMetadata fileObjectMetadata) throws IOException {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName, buildObjectMetadata(fileObjectMetadata));
        return initiateMultipartUpload(request, fileObjectMetadata);
    }
    
    /**
     * 初始化分片上传请求
     * @param request
     * @param fileObjectMetadata
     * @return
     * @throws IOException 
     */
    public InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadRequest request, @NotNull FileObjectMetadata fileObjectMetadata) throws IOException {
        beforeUploadFilter(request);
        InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);
        afterUploadFilter(request);
        return result;
    }
    
    /**
     * 分片上传
     * @param bucketName    桶
     * @param objectName    文件名称
     * @param uploadId      分片上传ID
     * @param is            分片文件流
     * @param partNumber    第几分片，从1开始，最大10000
     * @return
     * @throws IOException
     */
    public UploadPartResult uploadPart(String bucketName, String objectName, String uploadId, InputStream is, int partNumber) throws IOException {
        UploadPartRequest request = new UploadPartRequest();
        request.setBucketName(bucketName);
        request.setKey(objectName);
        request.setUploadId(uploadId);
        
        request.setInputStream(is);
        // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为10 KB。
        request.setPartSize(is.available());
        // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
        request.setPartNumber(partNumber);
        return uploadPart(request);
    }
    
    /**
     * 分片上传
     * @param request
     * @return
     */
    public UploadPartResult uploadPart(UploadPartRequest request) {
        return client.uploadPart(request);
    }
    
    /**
     * 合并分片
     * @param bucketName    桶
     * @param objectName    文件名称           
     * @param uploadId      分片上传ID
     * @param partETags     分片列表
     * @return
     * @throws IOException 
     */
    public CompleteMultipartUploadResult completeMultipartUpload(String bucketName, String objectName, String uploadId, List<PartETag> partEtags) throws IOException {
        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partEtags);
        return completeMultipartUpload(request);
    }
    
    /**
     * 合并分片
     * @param request   合并分片
     * @return
     * @throws IOException 
     */
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request) throws IOException {
        beforeUploadFilter(request);
        CompleteMultipartUploadResult result = client.completeMultipartUpload(request);
        afterUploadFilter(request);
        return result;
    }

    /***------ 分片上传API 结束 ------***/
    
    /**
     * 获取对象标签
     * @param bucketName    桶
     * @param objectName    对象名称
     */
    public List<Tag> getObjectTagging(String bucketName, String objectName) {
        return client.getObjectTagging(new GetObjectTaggingRequest(bucketName, objectName)).getTagSet();
    }
    
    /**
     * 设置对象标签（对象的标签会被该集合替换）
     * @param bucketName    桶
     * @param objectName    对象名称
     * @param tagSet        标签集合
     */
    public void setObjectTagging(String bucketName, String objectName, List<Tag> tagSet) {
        SetObjectTaggingRequest request = new SetObjectTaggingRequest(bucketName, objectName, new ObjectTagging(tagSet));
        client.setObjectTagging(request);
    }
    
    /**
     * 删除对象所有标签
     * @param bucketName    桶
     * @param objectName    文件名
     */
    public void deleteObjectTagging(String bucketName, String objectName) {
        DeleteObjectTaggingRequest request = new DeleteObjectTaggingRequest(bucketName, objectName);
        client.deleteObjectTagging(request);
    }
    
    /**
     * 获取生命周期策略
     * @param bucketName    桶
     * @return
     */
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(String bucketName) {
        return client.getBucketLifecycleConfiguration(bucketName);
    }
    
    /**
     * 设置生命周期策略（桶的所有生命周期策略会被该策略替换）
     * @param bucketName    桶
     * @param config        生命周期策略
     */
    public void setBucketLifecycleConfiguration(String bucketName, BucketLifecycleConfiguration config) {
        client.setBucketLifecycleConfiguration(bucketName, config);
    }
    
    /**
     * 执行上传过滤器 
     * @param obj
     * @throws IOException 
     */
    private void beforeUploadFilter(Object obj) throws IOException {        
        for (UploadFilter filter : uploadFilters) {
            if (obj instanceof PutObjectRequest) {
                filter.beforeUpload((PutObjectRequest) obj);
            } else if (obj instanceof InitiateMultipartUploadRequest) {
                filter.beforeUpload((InitiateMultipartUploadRequest) obj);
            } 
        }
        // 设置文件用户提供的元数据，以及 Amazon S3 发送和接收的标准 HTTP 标头
        ObjectMetadata metadata = null;
        if (obj instanceof PutObjectRequest) {
            PutObjectRequest request = ((PutObjectRequest) obj);
            metadata = request.getMetadata();
            if (metadata == null) {
                metadata = new ObjectMetadata();
            }
            request.setMetadata(metadata);
            
            if (metadata.getContentType() == null) {
                File file = request.getFile();
                if (request.getFile() != null) {
                    metadata.setContentType(Files.probeContentType(file.toPath()));
                }
            }
            if (metadata.getContentLength() <= 0) {
                File file = request.getFile();
                if (request.getFile() != null) {
                    metadata.setContentLength(file.length());
                }
                if (request.getInputStream() != null) {
                    metadata.setContentLength(request.getInputStream().available());
                }
            }
            
        } else if (obj instanceof InitiateMultipartUploadRequest) {
            InitiateMultipartUploadRequest request = (InitiateMultipartUploadRequest) obj;
            metadata = request.getObjectMetadata();
            if (metadata == null) {
                metadata = new ObjectMetadata();
            }
            request.setObjectMetadata(metadata);           
        }
    }
    
    /**
     * 执行上传后过滤器 
     * @param obj
     */
    private void afterUploadFilter(Object obj) {
        for (UploadFilter filter : uploadFilters) {
            if (obj instanceof PutObjectRequest) {
                filter.afterUpload((PutObjectRequest) obj);
            } else if (obj instanceof CompleteMultipartUploadRequest) {
                filter.afterUpload((CompleteMultipartUploadRequest) obj);
            } 
        }
    }
    
    public String getBucketName() {
        return bucketName;
    }
    public String getEndpoint() {
        return endpoint;
    }
    public void addUploadFilter(UploadFilter filter) {
        this.uploadFilters.add(filter);
        logger.info("--->>> add UploadFilter:{},order:{}", filter.getClass().getName(), filter.getOrder());
    }
    public AmazonS3 getClient() {
        return client;
    }
    public ObjectMetadata buildObjectMetadata(FileObjectMetadata fileObjectMetadata) {
        ObjectMetadata metadata = new ObjectMetadata();
        if (fileObjectMetadata != null) {
            Field field = ReflectionUtils.findField(ObjectMetadata.class, "metadata");
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, metadata, fileObjectMetadata.getMetadata());
        }
        return metadata;
    }

}
