package com.fqm.framework.common.file.minio.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.common.file.FileService;
import com.fqm.framework.common.file.minio.MinioService;
import com.fqm.framework.common.file.minio.impl.FileServiceMinioImpl;

import io.minio.MinioClient;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
// name=enable 且值为true，时才加载，matchIfMissing=true 缺失也加载
@ConditionalOnProperty(prefix = "minio", name = "enable", havingValue = "true")
@EnableConfigurationProperties({MinioProperties.class})
public class MinioAutoConfig {

    private final MinioProperties properties;

    public MinioAutoConfig(MinioProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MinioClient getMinioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(this.properties.getEndpoint(), this.properties.getPort(), this.properties.getSecure())
                .credentials(this.properties.getAccessKey(), this.properties.getSecretKey())
                .build();
        return minioClient;
    }
    
    @Bean
    public MinioService getMinioService(MinioClient minioClient) {
        return new MinioService(minioClient);
    }
    
    @Bean("fileServiceMinio")
    @ConditionalOnMissingBean(value = FileServiceMinioImpl.class)
    public FileService getFileServiceMinio() {
        return new FileServiceMinioImpl();
    }
    
}
