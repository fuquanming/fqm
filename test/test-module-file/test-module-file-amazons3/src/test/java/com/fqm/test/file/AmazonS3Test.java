package com.fqm.test.file;

import java.io.File;
import java.util.Arrays;

import com.fqm.framework.file.amazons3.AmazonS3Service;
import com.fqm.framework.file.model.FileUploadRequest;
import com.fqm.framework.file.template.AmazonS3FileTemplate;

public class AmazonS3Test {

    public static void main(String[] args) {
        // minio
        String endpoint = "http://192.168.86.145:11000";
        String accessKey = "admin";
        String secretKey = "12345678";
        String bucketName = "amazon-s3";
        String region = "";
        String accessUrl = "http://192.168.86.145/s3/";
        // aliyun
        endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        accessKey = "";
        secretKey = "";
        bucketName = "fqm1";
        region = "";
        accessUrl = "";
        
        testFile(endpoint, accessKey, secretKey, bucketName, region, accessUrl);
    }

    private static void testFile(String endpoint, String accessKey, String secretKey, String bucketName, String region, String accessUrl) {
        AmazonS3Service service = new AmazonS3Service(endpoint, accessKey, secretKey, bucketName, region);
        AmazonS3FileTemplate template = new AmazonS3FileTemplate(service, accessUrl);
        
        File uploadFile = new File("C:\\Users\\fqm\\Pictures\\a.png");
        String fileId = template.uploadFile(uploadFile, "s3/a.png");
        
        File downloadFile = new File("C:\\Users\\fqm\\Pictures\\a-1.png");
        template.downloadFile(fileId, downloadFile.getPath());
        
//        String fileId = "s3/a.png";
        System.out.println(template.getFileUrlExpires(fileId, 60));
        
        System.out.println(template.getFileUrl(fileId));
        
        System.out.println(template.deleteFile(Arrays.asList("a-1.png","test.png")));
//        
        template.deleteDir("s3/");
    }
    
    
}
