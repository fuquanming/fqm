package com.fqm.framework.file.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.file.FileFactory;
import com.fqm.framework.file.amazons3.AmazonS3Service;
import com.fqm.framework.file.template.AmazonS3FileTemplate;

/**
 * 亚马逊 s3 协议存储文件
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnProperty(name = "amazons3.enabled", havingValue = "true")
public class AmazonS3FileAutoConfiguration {

    @Bean
    @ConfigurationProperties("amazons3")
    AmazonS3Properties amazonS3Properties() {
        return new AmazonS3Properties();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    AmazonS3FileTemplate amazonS3FileTemplate(FileFactory fileFactory, AmazonS3Properties amazonS3Properties) {
        AmazonS3FileTemplate amazonS3FileTemplate = new AmazonS3FileTemplate(
                new AmazonS3Service(amazonS3Properties.getEndpoint(), 
                        amazonS3Properties.getAccessKey(), 
                        amazonS3Properties.getSecretKey(), 
                        amazonS3Properties.getBucketName(), 
                        amazonS3Properties.getRegion()), amazonS3Properties.getAccessUrl());
        fileFactory.addFileTemplate(amazonS3FileTemplate);
        return amazonS3FileTemplate;
    }
    
}
