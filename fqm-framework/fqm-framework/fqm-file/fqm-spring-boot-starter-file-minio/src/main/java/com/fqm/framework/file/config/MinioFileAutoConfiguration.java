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
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
// name=enable 且值为true，时才加载，matchIfMissing=true 缺失也加载
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({ MinioProperties.class })
public class MinioFileAutoConfiguration {

    private final MinioProperties properties;

    public MinioFileAutoConfiguration(MinioProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioClient getMinioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(this.properties.getEndpoint(), this.properties.getPort(), this.properties.getSecure())
                .credentials(this.properties.getAccessKey(), this.properties.getSecretKey()).build();
        return minioClient;
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
