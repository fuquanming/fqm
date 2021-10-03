# 简介
MultilevelCache 是一个Java缓存抽象，它为各种缓存解决方案提供一致的使用。它扩展了springcache注释。
MultilevelCache 中的注释支持本地TTL，

两级缓存，分布式自动刷新，也可以手工代码操作```Cache```实例。

目前有四种实现：```RedisCache```（Jedis，Lettuce），```RedissonCache```（Redisson），```caffineCache```（内存中），。

MultiLevelCache的全部功能：

* 通过一致的缓存API操作缓存 
* 声明性方法缓存，使用带有TTL（生存时间）和两级缓存支持的注释 
* Spring Boot support
* `RedisCache` 支持 Redis 删除 key 时，级联删除本地缓存

要求:
* JDK1.8
* Spring Framework4.0.8+ (可选)
* Spring Boot1.1.9+ (可选)

# 入门

## 方法缓存
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

### redis

pom:

```xml
<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-multilevel-cache-redis</artifactId>
    <version>${multilevelCache.latest.version}</version>
</dependency>
```

App class:
```java
@SpringBootApplication
@EnableMethodCache(basePackages = "com.company.mypackage")
@@EnableCaching
public class MySpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApp.class);
    }
    
    /** 自定义Redis序列化，选填，默认jdk */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))             .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return defaultCacheConfig;
    }
}
```

spring boot application.yml config:
```yaml
spring:  
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
#    password: 
    timeout: 6000
    jedis:
      pool:
        #连接池最大的连接数，若使用负值表示没有限制
        max-active: 8
        #连接池最大阻塞等待时间，若使用负值表示没有限制
        max-wait: -1
        #连接池中的最大空闲连接
        max-idle: 8
        #连接池中的最小空闲连接
        min-idle: 0
    lettuce:
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0
```

### redisson

pom:

```xml
<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-multilevel-cache-redisson</artifactId>
    <version>${multilevelCache.latest.version}</version>
</dependency>
```

App class:

```java
@SpringBootApplication
@EnableMethodCache(basePackages = "com.company.mypackage")
@@EnableCaching
public class MySpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApp.class);
    }
    
    /** 自定义Redisson序列化，选填，默认JsonJacksonCodec */
	@Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec()); //默认
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);
        return client;
    }
}
```

spring boot application.yml config: 参考redis

### 自定义缓存管理

~~~java
@Configuration
public class MultiLevelCacheManagerConfig {
    @Bean(name = "multilevelCache", destroyMethod = "destroy")
    public MultiLevelCacheManager multiLevelCacheManager() {
        MultiLevelCacheManager cacheManager = new MultiLevelCacheManager();
        List<CacheBuilder> cacheBuilders = new ArrayList<CacheBuilder>();
        // 添加多个缓存实现类...
        cacheBuilders.add(new CaffeineCacheBuilders());
        cacheManager.setCacheBuilders(cacheBuilders);
        return cacheManager;
    }
}
~~~

