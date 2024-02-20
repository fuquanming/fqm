package com.fqm.test.file;

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
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilterPredicate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class Aws3ConfigTest {
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
//        bucketName = "hotel-dev";// 无桶策略
        
        
        AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        AmazonS3ClientBuilder build = AmazonS3Client.builder().withEndpointConfiguration(endpointConfig).withCredentials(credentialsProvider)
                .disableChunkedEncoding();
        AmazonS3 client = build.build();
        
        // 获取桶策略
        BucketLifecycleConfiguration bucketLifecycleConfiguration = client.getBucketLifecycleConfiguration(bucketName);
        System.out.println("bucketLifecycleConfiguration=" + bucketLifecycleConfiguration);
        List<Rule> rules = bucketLifecycleConfiguration.getRules();
        System.out.println("rules=" + rules.size());
        for (Rule r : rules) {
            System.out.println(r.getId());
            LifecycleFilter f = r.getFilter();
            LifecycleFilterPredicate predicate = f.getPredicate();
            System.out.println(predicate);
        }
    }

}
