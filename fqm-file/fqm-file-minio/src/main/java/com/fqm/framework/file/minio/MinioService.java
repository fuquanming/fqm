package com.fqm.framework.file.minio;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.reflect.FieldUtils;

import io.minio.BucketExistsArgs;
import io.minio.DeleteObjectTagsArgs;
import io.minio.DownloadObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.SetObjectTagsArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import io.minio.messages.Tags;
import okhttp3.HttpUrl;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class MinioService {

    /** 默认过期时间7天 */
    private static final int DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;

    private MinioClient minioClient;

    /** 对象名称第一个字符 */
    private String objectNameFirstCharacter = "/";

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 检查存储桶是否存在
     * @param bucketName 存储桶名称
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */
    public boolean bucketExists(String bucketName) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储桶
     * @param bucketName 存储桶名称
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public boolean makeBucket(String bucketName) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return false;
        } else {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            return true;
        }
    }

    /**
     * 删除存储桶
     * @param bucketName 存储桶名称
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public boolean removeBucket(String bucketName) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                // 有对象文件，则删除失败
                if (item.size() > 0) {
                    return false;
                }
            }
            // 删除存储桶，注意，只有存储桶为空时才能删除成功。
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            flag = bucketExists(bucketName);
            if (!flag) {
                return true;
            }
        }
        return false;
    }

    /**
     * 列出所有存储桶名称
     * @return
     * @throws IOException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */
    public List<String> listBucketNames() throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IOException {
        List<Bucket> bucketList = listBuckets();
        List<String> bucketListName = new ArrayList<>(bucketList.size());
        for (Bucket bucket : bucketList) {
            bucketListName.add(bucket.name());
        }
        return bucketListName;
    }

    /**
     * 列出所有存储桶
     * @return
     * @throws IOException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public List<Bucket> listBuckets() throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
            InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IOException {
        return minioClient.listBuckets();
    }

    /**
     * 列出存储桶中的所有对象名称
     * @param bucketName 存储桶名称
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public List<String> listObjectNames(String bucketName)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        List<String> listObjectNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                listObjectNames.add(item.objectName());
            }
        }
        return listObjectNames;
    }

    /**
     * 列出存储桶中的所有对象
     * @param bucketName 存储桶名称
     * @return
     */

    public Iterable<Result<Item>> listObjects(String bucketName) {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 列出存储桶中的所有对象
     * @param bucketName 存储桶名称
     * @param prefix     前缀
     * @param after      后缀
     * @param maxKeys    最大数量
     * @return
     */

    public Iterable<Result<Item>> listObjects(String bucketName, String prefix, String after, int maxKeys) {
        ListObjectsArgs.Builder builder = ListObjectsArgs.builder().bucket(bucketName);
        if (prefix != null && prefix.length() > 0) {
            builder.prefix(prefix);
        }
        if (after != null && after.length() > 0) {
            builder.startAfter(after);
        }
        if (maxKeys > 0) {
            builder.maxKeys(maxKeys);
        }
        return minioClient.listObjects(builder.build());
    }

    /**
     * 删除对象tag信息
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public void deleteObjectTags(String bucketName, String objectName)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        minioClient.deleteObjectTags(DeleteObjectTagsArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 文件上传（已知文件大小）
     * @param bucketName  存储桶名称
     * @param objectName  存储桶里的对象名称
     * @param stream      文件流
     * @param size        大小
     * @param contentType 文件类型
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */
    public boolean putObject(String bucketName, String objectName, InputStream stream, long size, String contentType)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        minioClient
                .putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(stream, size, -1).contentType(contentType).build());
        StatObjectResponse statObject = statObject(bucketName, objectName);
        return null != statObject && statObject.size() > 0;
    }

    /**
     * 文件上传（已知文件大小）
     * @param bucketName  存储桶名称
     * @param objectName  存储桶里的对象名称
     * @param stream      文件流
     * @param size        大小
     * @param contentType 文件类型
     * @param headers     文件headers
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public boolean putObject(String bucketName, String objectName, InputStream stream, long size, String contentType, Map<String, String> headers)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(stream, size, -1).headers(headers).tags(headers)
                        .contentType(contentType).build());
        StatObjectResponse statObject = statObject(bucketName, objectName);
        return null != statObject && statObject.size() > 0;
    }

    /**
     * 通过InputStream上传对象
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param stream     要上传的流
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public boolean putObject(String bucketName, String objectName, InputStream stream)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(stream, stream.available(), -1).build());
            StatObjectResponse statObject = statObject(bucketName, objectName);
            if (statObject != null && statObject.size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 以流的形式获取一个文件对象
     * 需要释放stream资源
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public InputStream getObject(String bucketName, String objectName)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 以流的形式获取一个文件对象（断点下载）
     * 需要释放stream资源
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param offset     起始字节的位置
     * @param length     要读取的长度 (可选，如果无值则代表读到文件结尾)
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public InputStream getObject(String bucketName, String objectName, long offset, Long length)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).offset(offset).length(length).build());
    }

    /**
     * 获取对象的tags
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */
    public Tags getObjectTags(String bucketName, String objectName) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        return minioClient.getObjectTags(GetObjectTagsArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 删除一个对象
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public boolean removeObject(String bucketName, String objectName)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        return true;
    }

    /**
     * 删除指定桶的多个文件对象,返回删除错误的对象列表，全部删除成功，返回空列表
     * @param bucketName  存储桶名称
     * @param objectNames 含有要删除的多个object名称的迭代器对象
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public List<String> removeObjects(String bucketName, List<String> objectNames)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        List<String> deleteErrorNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);
        if (flag) {
            List<DeleteObject> list = new LinkedList<>();
            objectNames.forEach(item -> list.add(new DeleteObject(item)));

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(list).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                deleteErrorNames.add(error.objectName());
            }
        }
        return deleteErrorNames;
    }

    /**
     * 给文件添加tags
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param tags       标签
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public void setObjectTags(String bucketName, String objectName, Map<String, String> tags)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        minioClient.setObjectTags(SetObjectTagsArgs.builder().bucket(bucketName).object(objectName).tags(tags).build());
    }

    /**
     * 生成一个给HTTP GET请求用的presigned URL。
     * 浏览器/移动端的客户端可以用这个URL进行下载，即使其所在的存储桶是私有的。这个presigned URL可以设置一个失效时间，默认值是7天。
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param expires    失效时间（以秒为单位），默认是7天，不得大于七天
     * @return
     * @throws MinioException 
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */

    public String getPresignedObjectUrl(String bucketName, String objectName, Integer expires, Method method)
            throws MinioException, InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, IOException {
        String url = "";
        if (expires < 1 || expires > DEFAULT_EXPIRY_TIME) {
            throw new io.minio.errors.MinioException("expires must be in range of 1 to " + DEFAULT_EXPIRY_TIME);
        }
        if (method == null) {
            method = Method.GET;
        }
        url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder().method(method).bucket(bucketName).object(objectName).expiry(expires, TimeUnit.SECONDS).build());
        return url;
    }

    /**
     * 获取对象的元数据
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public StatObjectResponse statObject(String bucketName, String objectName)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        return minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 文件访问路径
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     * @throws IllegalAccessException 
     */
    public String getObjectUrl(String bucketName, String objectName) throws IllegalAccessException {
        MinioAsyncClient asyncClient = (MinioAsyncClient) FieldUtils.readField(minioClient, "asyncClient", true);
        HttpUrl baseUrl = (HttpUrl) FieldUtils.readField(asyncClient, "baseUrl", true);
        StringBuilder data = new StringBuilder(baseUrl.toString());
        data.append(bucketName);
        if (!objectName.startsWith(objectNameFirstCharacter)) {
            data.append(objectNameFirstCharacter);
        }
        data.append(objectName);
        return data.toString();
    }

    /**
     * 下载文件，在项目根目录
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param fileName   下载后文件名称
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */

    public void downloadObject(String bucketName, String objectName, String fileName)
            throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
            NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucketName).object(objectName).filename(fileName).build());
    }
}
