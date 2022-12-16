package com.fqm.framework.file.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.file.FileFactory;
import com.fqm.framework.file.minio.MinioService;
import com.fqm.framework.file.template.MinioFileTemplate;

import io.minio.MinioClient;

/**
 * 
 * name=enable 且值为true，时才加载，matchIfMissing=true 缺失也加载
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
@EnableConfigurationProperties({ MinioProperties.class })
public class MinioFileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint(), properties.getPort(), properties.getSecure())
                .credentials(properties.getAccessKey(), properties.getSecretKey()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public MinioFileTemplate minioFileTemplate(FileFactory fileFactory, MinioClient minioClient, MinioProperties minioProperties) {
        MinioFileTemplate minioFileTemplate = new MinioFileTemplate(new MinioService(minioClient), minioProperties.getBucketDefaultName());
        fileFactory.addFileTemplate(minioFileTemplate);
        return minioFileTemplate;
    }

}
