# 简介
要求:
* JDK1.8
* Spring Framework4.0.8+ (可选)
* Spring Boot1.1.9+ (可选)

# 入门

## 使用MinIo做文件服务器
## Spring Boot 配置

### MinIO

pom:

```xml
<properties>	
	<okhttp3.version>4.8.1</okhttp3.version>
</properties>

<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-file-minio</artifactId>
    <version>${latest.version}</version>
</dependency>
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>${okhttp3.version}</version>
</dependency>
```

App class:
```java
@Resource
FileService fileService;
public R<String> uploadFileService {
	String fileId = fileService.uploadFile(file, "test/" + file.getName());// 上传的文件名
    return R.ok(fileId);
}
```

spring boot application.yml config:
```yaml
# 文件服务  
minio:
  enabled: true			# 是否开启文件服务
  endpoint: 127.0.0.1
  port: 11000			# 文件服务端口
  accessKey: admin
  secretKey: 12345678
  secure: false
  bucketDefaultName: file
```

