# 简介
要求:
* JDK1.8
* Spring Framework4.0.8+ (可选)
* Spring Boot1.1.9+ (可选)

# 入门

## 使用MinIo做文件服务器
使用“```@Cacheable```”批注声明方法缓存，支持表达式、配置文件读取，类似@Value功能

value = "user|12|5|9"

- 12：过期时间12秒
- 5：null值过期时间5秒
- 9：自动刷新时间9秒

value = "user|${cache.user.expire}|${cache.user.nullExpire}|${cache.user.refresh}"

- ${cache.user.expire}：过期时间
- ${cache.user.nullExpire}：null值过期时间
- ${cache.user.refresh}：自动刷新时间

sync = true

- 缓存穿透保护

cacheManager = "multilevelCacheRedis"

- 指定缓存管理

MultilevelCache 使用所有参数自动生成缓存密钥。

```java
public interface UserService {
    @Cacheable(value = "user|12|5|9", sync = true)
    User getUserById(long userId);
    
    @Cacheable(value = "user|${cache.user.expire}|${cache.user.nullExpire}|${cache.user.refresh}", sync = true)
    User getUserById2(long userId);
}
```

使用“```key```“属性指定缓存密钥，使用[SpEL]
```java
public interface UserService {
    @Cacheable(value = "user|12|9", key = "'user_id_' + #userId", sync = true)
    User getUserById(long userId);
    
    @Cacheable(value = "user|${cache.user.expire}|${cache.user.nullExpire}|${cache.user.refresh}", key = "'user_id_' + #userId", sync = true)
    User getUserById2(long userId);
}
```
使用“```cacheManager```“属性指定缓存管理，使用[SpEL]

```java
public interface UserService {
    @Cacheable(value = "user|12|5|9", key = "'user_id_' + #userId", sync = true, , cacheManager = "multilevelCacheRedis")
    User getUserById(long userId);
    
    @Cacheable(value = "user|${cache.user.expire}|${cache.user.nullExpire}|${cache.user.refresh}", key = "'user_id_' + #userId", sync = true, cacheManager = "multilevelCacheRedis")
    User getUserById2(long userId);
}
```

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
  enable: true			# 是否开启文件服务
  endpoint: 127.0.0.1
  port: 9000
  accessKey: admin
  secretKey: 12345678
  secure: false
  bucketDefaultName: file
```

