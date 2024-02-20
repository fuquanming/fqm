package com.fqm.test.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Rule;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Transition;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.lifecycle.LifecycleAndOperator;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilterPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecyclePrefixPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class Aws3Test {
    // 设置日志级别
    static {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
        loggerList.forEach(logger -> {
            logger.setLevel(Level.INFO);
        });
    }
    
    public static void main(String[] args) {
        String endpoint = "http://172.16.50.105:11000";
        String accessKey = "admin";
        String secretKey = "W7GLCvcUnPaeRbeW";
        String region = "";
        String bucketName = "test";
        
//        accessKey = "testuser";
//        secretKey = "testuser";
//        
//        
//        endpoint = "http://172.16.50.71:9000";
//        accessKey = "5CJZpP5dcnE8M3vv";
//        secretKey = "oBgY9H3qoXqaCGuTuvNQ9z2gEcf3xWIJ";
//        region = "";
//        bucketName = "searhyme";
        

//        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
//        AmazonS3 client = AmazonS3ClientBuilder.standard()
//                .withCredentials(
//                        new AWSStaticCredentialsProvider(awsCreds)).withRegion(region).build();
        AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AmazonS3ClientBuilder build = AmazonS3Client.builder().withEndpointConfiguration(endpointConfig).withCredentials(credentialsProvider)
                .disableChunkedEncoding();
//        AmazonS3Client client = (AmazonS3Client) build.build();
        AmazonS3 client = build.build();
        
        File file = new File("C:\\Users\\fqm\\Pictures\\a.png");
        String filepath = "test/1.png";
        
        // 删除对象tag
//        client.deleteObjectTagging(new DeleteObjectTaggingRequest(bucketName, filepath));
        
//        filepath = "2024/01/31/Spring Cloud Alibaba笔记_8d3a7be7ab638463eb2af832f5916fdc.pdf";
        
        // 上传对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filepath, file);
        // 设置上传对象的 Acl 为公共读
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
        Tag tag = new Tag("expiry", "7");
        putObjectRequest.setTagging(new ObjectTagging(Collections.singletonList(tag)));
        client.putObject(putObjectRequest);

        // 获取tag
        GetObjectTaggingResult objectTagging = client.getObjectTagging(
                new GetObjectTaggingRequest(bucketName, filepath));
        List<Tag> tagSet = objectTagging.getTagSet();
        if (tagSet != null) {
            for (Tag t : tagSet) {
                System.out.println(t.getKey() + ":" + t.getValue());
            }
        }
        
        
        // 修改 tag
        Tag newTag = new Tag("expiry", "7");  
        // 将更新后的 ACL 重新应用到对象  
        ObjectTagging tags = new ObjectTagging(Collections.singletonList(newTag));
        
//        tagSet.add(newTag);
//        ObjectTagging tags = new ObjectTagging(tagSet);
        client.setObjectTagging(new SetObjectTaggingRequest(bucketName, filepath, tags));
        
        
        
        
        // 添加生命周期规则：所有文件有tag为expiry:7 的1天后删除
        BucketLifecycleConfiguration config = new BucketLifecycleConfiguration();
        Rule rule = new Rule();
        rule.setId("22");
        rule.setExpirationInDays(1);
//        rule.setTransition(new Transition());
//        rule.setTransitions(Collections.singletonList(new Transition()));
        rule.setStatus(BucketLifecycleConfiguration.ENABLED);
        
        LifecycleFilter filter = new LifecycleFilter();
        LifecycleTagPredicate tagp = new LifecycleTagPredicate(new Tag("expiry", "7")); 
        LifecyclePrefixPredicate pre = new LifecyclePrefixPredicate("");
        
        List<LifecycleFilterPredicate> list = new ArrayList<>();
        list.add(pre);
        list.add(tagp);
        filter.setPredicate(new LifecycleAndOperator(list));
        
        rule.setFilter(filter);
        
        config.setRules(Collections.singletonList(rule));
        
        SetBucketLifecycleConfigurationRequest req = new SetBucketLifecycleConfigurationRequest(bucketName, config);
        client.setBucketLifecycleConfiguration(req);
     
        // 获取桶规则
        BucketLifecycleConfiguration bucketLifecycleConfiguration = client.getBucketLifecycleConfiguration(bucketName);
        List<Rule> rules = bucketLifecycleConfiguration.getRules();
        for (Rule r : rules) {
            System.out.println(r.getId());
            LifecycleFilter f = r.getFilter();
            LifecycleFilterPredicate predicate = f.getPredicate();
            System.out.println(predicate);
        }
        
    }
}
