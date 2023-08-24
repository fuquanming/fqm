package com.fqm.framework.file.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.file.FileMode;
import com.fqm.framework.file.amazons3.AmazonS3Service;
import com.fqm.framework.file.enums.FileUploadStatus;
import com.fqm.framework.file.model.FileUploadRequest;
import com.fqm.framework.file.model.FileUploadResponse;


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
    
    private StringRedisTemplate stringRedisTemplate;
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
    
    public AmazonS3FileTemplate(AmazonS3Service service, String accessUrl, StringRedisTemplate stringRedisTemplate) {
        this.service = service;
        this.bucketName = service.getBucketName();
        this.accessUrl = accessUrl;
        this.stringRedisTemplate = stringRedisTemplate;
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
        boolean flag = service.putObject(bucketName, fileName, file);
        return flag ? fileName : null;
    }

    @Override
    public String uploadFile(InputStream is, String fileName) {
        try {
            boolean flag = service.putObject(bucketName, fileName, is);
            return flag ? fileName : null;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("File upload s3 IOException", e);
        }
        return null;
    }
    
    @Override
    public FileUploadResponse uploadFile(FileUploadRequest fileUploadRequest, File file, String fileName) {
        try (InputStream is = new FileInputStream(file)) {
            return uploadFile(fileUploadRequest, is, fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("File upload s3 FileNotFoundException", e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("File upload s3 IOException", e);
        }
        return null;
    }
    
    /**
     * 存储分片上传成功时分片信息
     * 1)被删除了uploadId，有分片失败 partList -> -2
     * 2)本次分片失败  partList -> -1
     */
    @SuppressWarnings("rawtypes")
    private static RedisScript<List> scriptPartTag = new DefaultRedisScript<>(
            "local function getAll(key) "
                    + "    local result = {} "
                    + "    local map = redis.call(\"hgetall\", key) "
                    + "    for i = 1, #map, 2 do "
                    + "        if map[i] ~= 'uploadId' then table.insert(result, map[i + 1]) end "
                    + "    end "
                    + "    return result "
                    + "end " 
                    + "local partList = {} "
                    + "if redis.call('hexists',KEYS[1],'uploadId') ~=1 then table.insert(partList, -2) return partList end "
//                    + "if redis.call('hset',KEYS[1],KEYS[2],ARGV[1]) ~=1 then table.insert(partList, -1) return partList end "
                    + "redis.call('hset',KEYS[1],KEYS[2],ARGV[1]) "
                    + "redis.call('expire',KEYS[1],600) "
                    + "local len = redis.call('hlen', KEYS[1]) "
                    + "if (len == tonumber(ARGV[2])) then "
                        + "local key = KEYS[1] "
                        + "return getAll(key) "
                    + "end "
                    + "return partList", List.class);
    /**
     * 分片上传失败
     */
    private int partTagSelfError = -1;
    /**
     * 其他分片上传失败
     */
    private int partTagOtherError = -2;
    
    /**
     * 实现方式：成功才删除缓存；重传则继续，否则等超时（60s）
     * @see com.fqm.framework.file.template.FileTemplate#uploadFile(com.fqm.framework.file.model.FileUploadRequest, java.io.InputStream, java.lang.String)
     *
     */
    @Override
    public FileUploadResponse uploadFile(FileUploadRequest fileUploadRequest, InputStream is, String fileName) {
        FileUploadResponse response = new FileUploadResponse();
        if (isFileChunkUpload(fileUploadRequest)) {
            response.setChunk(fileUploadRequest.getChunk());
            response.setChunks(fileUploadRequest.getChunks());
            response.setSize(fileUploadRequest.getSize());
            response.setMd5(fileUploadRequest.getMd5());
            response.setRequestId(fileUploadRequest.getRequestId());
            
            String fileKey = "";
            try {
                String key = StringUtils.isNoneBlank(fileUploadRequest.getRequestId()) ? fileUploadRequest.getRequestId() : fileUploadRequest.getMd5();
                if (StringUtils.isBlank(key)) {
                    return getErrorResponse(response, "requestId 或 md5，两者必须填写一项 ");
                }
                fileKey = "mufi_" + key;
                // 1.获取uploadId
                String uploadId = getUploadIdByFileChunk(fileKey, fileName);
                // 2.上传分片
                if (StringUtils.isBlank(uploadId)) {
                    return getErrorResponse(response, "生成uploadId异常");
                }
                response.setUploadId(uploadId);
                response.setChunkSize(is.available());
                
                UploadPartResult uploadPartResult = uploadPart(fileUploadRequest, is, fileName, uploadId);
                PartETag partTag = uploadPartResult.getPartETag();
                String partTagJson = JsonUtil.toJsonStr(partTag);
                
                uploadPartToCache(fileUploadRequest, fileName, response, fileKey, uploadId, partTagJson); 
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                getErrorResponse(response, e.getMessage());
                logger.error("File uploadFileChunk s3 Exception", e);
            } finally {
                // 成功则删除redis缓存数据
                if (FileUploadStatus.SUCCESS == response.getUploadStatus()) {
                    logger.error("delete redis->{}", fileKey);
                    stringRedisTemplate.delete(fileKey);
                }
                if (FileUploadStatus.FAIL == response.getUploadStatus()) {
                    String str = JsonUtil.toJsonStr(response);
                    logger.error("File uploadFileChunk s3 chunkFail {}", str);
                }
            }
            return response;
        }
        String fileId = uploadFile(is, fileName);
        response.setFileId(fileId);
        response.setUploadStatus(fileId == null ? FileUploadStatus.FAIL : FileUploadStatus.SUCCESS);
        response.setChunkUploadStatus(null != fileId);
        return response;
    }

    /**
     * 3.分片信息存储到缓存，最后一个分片上传完成后，返回所有分片信息
     * @param fileUploadRequest
     * @param fileName
     * @param response
     * @param fileKey
     * @param uploadId
     * @param partTagJson
     */
    private FileUploadResponse uploadPartToCache(FileUploadRequest fileUploadRequest, String fileName, FileUploadResponse response, String fileKey, String uploadId,
            String partTagJson) {
        // 3.分片信息存储到缓存，最后一个分片上传完成后，返回所有分片信息
        // map[i] 是key map[i + 1] 是value
        // 推送到redis及，最后分片上传时返回分片列表。
        List<?> mapList = stringRedisTemplate.execute(
                scriptPartTag,
                Arrays.asList(fileKey, fileUploadRequest.getChunk() + ""),
                // 分片信息，map中size=分片总数+1
                partTagJson, (fileUploadRequest.getChunks() + 1) + "");
        // 3.1.有所有分片信息，则合并分片
        if (null != mapList && !mapList.isEmpty()) {
            // 一个-1，表示part上传失败
            if (mapList.size() == 1) {
                Object obj = mapList.get(0);
                if (!isJson(obj.toString())) {
                    int failFlag = Integer.parseInt(obj.toString());
                    if (failFlag == partTagSelfError) {
                        return getErrorResponse(response, "分片上传失败");
                    } else if (failFlag == partTagOtherError) {
                        return getErrorResponse(response, "其他分片上传失败");
                    }
                }
            }
            multipartUpload(fileName, uploadId, mapList);
            
            response.setChunkUploadStatus(true);
            response.setUploadStatus(FileUploadStatus.SUCCESS);
            response.setFileId(fileName);
        } else {
            // 分片上传完成
            response.setChunkUploadStatus(true);
            response.setUploadStatus(FileUploadStatus.UPLOADING);
        }
        return response;
    }

    private FileUploadResponse getErrorResponse(FileUploadResponse response, String msg) {
        response.setMsg(msg);
        response.setChunkUploadStatus(false);
        response.setUploadStatus(FileUploadStatus.FAIL);
        return response;
    }

    /**
     * 4.分片合并请求
     * @param fileName
     * @param uploadId
     * @param mapList
     */
    private void multipartUpload(String fileName, String uploadId, List<?> mapList) {
        // 最后分片上传完成
        List<PartETag> partList = new ArrayList<>(mapList.size());
        mapList.forEach( str -> {
            Map<?, ?> map = JsonUtil.toMap(str.toString());
            PartETag part = new PartETag(
                    Integer.valueOf(map.get("partNumber").toString()), 
                    map.get("etag").toString());
            partList.add(part);
        });
        // 排序
        partList.sort((f1,f2) ->  f1.getPartNumber() - f2.getPartNumber());
        
        // 4.返回分片上传结果
        // 判断是否最后一个分片，则提交合并请求
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
              new CompleteMultipartUploadRequest(bucketName, fileName, uploadId, partList);
        // 完成分片上传。
        service.getClient().completeMultipartUpload(completeMultipartUploadRequest);
    }

    /**
     * 2.上传分片
     * 分片上传-上传分片
     * @param fileUploadRequest
     * @param is
     * @param fileName
     * @param uploadId
     * @return
     * @throws IOException
     */
    private UploadPartResult uploadPart(FileUploadRequest fileUploadRequest, InputStream is, String fileName, String uploadId) throws IOException {
        UploadPartRequest uploadPartRequest = new UploadPartRequest();
        uploadPartRequest.setBucketName(bucketName);
        uploadPartRequest.setKey(fileName);
        uploadPartRequest.setUploadId(uploadId);
        
        uploadPartRequest.setInputStream(is);
        // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为10 KB。
        uploadPartRequest.setPartSize(is.available());
        // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
        uploadPartRequest.setPartNumber(fileUploadRequest.getChunk());
        // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
        return service.getClient().uploadPart(uploadPartRequest);
    }
    
    /**
     * 获取分片上传生成的uploadId
     */
    private static RedisScript<String> scriptUploadId = new DefaultRedisScript<>(
            "if redis.call('hexists',KEYS[1],'uploadId') == 1 then redis.call('expire',KEYS[1],600) return redis.call('hget',KEYS[1],'uploadId') end"
            + " if redis.call('hset',KEYS[1],'uploadId',ARGV[1]) == 1 then redis.call('expire',KEYS[1],600) return ARGV[1] else return '' end ", String.class);
    
    
    /**
     * 1.获取uploadId,分片上传（获取文件上传ID）60秒上传5M数据
     * @param fileUploadRequest
     * @return
     */
    private String getUploadIdByFileChunk(String fileMd5Key, String fileName) {
        // 获取缓存的uploadId
        BoundHashOperations<String, String, String> boundHashOps = stringRedisTemplate.boundHashOps(fileMd5Key);
        String uploadIdKey = "uploadId";
        String uploadId = boundHashOps.get(uploadIdKey);
        if (uploadId == null) {
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, fileName);
            InitiateMultipartUploadResult upresult = service.getClient().initiateMultipartUpload(request);
            uploadId = upresult.getUploadId();
            logger.debug("build UploadId={}", uploadId);
            // redis.call('expire',KEYS[1],60) setex
            // 推送到redis及，最后分片上传时返回分片列表。
            uploadId = stringRedisTemplate.execute(
                    scriptUploadId,
                    Arrays.asList(fileMd5Key),uploadId);
        }
        logger.debug("use uploadId={}", uploadId);
        return uploadId;
    }
    
    private boolean isJson(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        char firstChar = str.charAt(0);
        char lastChar = str.charAt(str.length() - 1);
        return (firstChar == '{' && lastChar == '}') || (firstChar == '[' && lastChar == ']');
    }
    
}
