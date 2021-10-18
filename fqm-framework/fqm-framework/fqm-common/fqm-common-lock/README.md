# 简介
fqm-common-lock 是一个Java分布式锁抽象。提供编程式及Lock4j注释。

目前有4种实现：

单机锁：`SimpleLock`

分布式锁：`RedissonLock`，`ZookeeperLock`，`RestTemplateLock`

fqm-common-lock的全部功能：

- 通过一致的锁API操作锁
- 声明性方法锁，使用带有锁的key、获取锁超时时间等支持的注释 
- Spring Boot support

要求:

* JDK1.8
* Spring Framework4.0.8+ (可选)
* Spring Boot1.1.9+ (可选)

# 入门

## 编程式

~~~java
// SimpleLockTemplate, 单机锁，基于ReentrantLock
// RedissonLockTemplate，分布式锁，基于org.redisson.api.RLock
// RedisTemplateLockTemplate，分布式锁，基于RedisTemplate
// ZookeeperLockTemplate，分布式锁，基于org.apache.curator.framework.recipes.locks.InterProcessMutex

public void testLock() {
    LockFactory lockFactory = new LockFactory();
    lockFactory.addLockTemplate(new SimpleLockTemplate());

    LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(SimpleLockTemplate.class);
    Lock lock = lockTemplate.getLock("123");
    try {
        lock.lock();
        // dosomething...
    } finally {
        lock.unlock();
    }
}

public void testTryLock() {
    LockFactory lockFactory = new LockFactory();
    lockFactory.addLockTemplate(new RedissonLockTemplate());

    LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(RedissonLockTemplate.class);
    Lock lock = lockTemplate.getLock("123");
    boolean lockFlag = false;
    try {
        lockFlag = lock.tryLock();
        // lock.tryLock(1000, TimeUtil.MILLISECONDS);
        // dosomething...
    } finally {
        if (!lockFlag) lock.unlock();
    }
}

public void testTryLockTime() {
    LockFactory lockFactory = new LockFactory();
    lockFactory.addLockTemplate(new ZookeeperLockTemplate());

    LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(ZookeeperLockTemplate.class);
    Lock lock = lockTemplate.getLock("123");
    boolean lockFlag = false;
    try {
        lockFlag = lock.tryLock(1000, TimeUtil.MILLISECONDS);
        // dosomething...
    } finally {
        if (!lockFlag) lock.unlock();
    }
}
~~~

## 注解式

~~~java
// block=true：调用lock()，阻塞线程，直到获取到锁为止，默认值为false
// key：支持表达式及配置文件，基于spring的@Value
@Lock4j(key = "${lock.user}", block = true, lockTemplate = SimplateLockTemplate.class)
~~~

~~~java
// 调用tryLock()，尝试获取一次锁
@Lock4j(key = "userId", lockTemplate = RedissonLockTemplate.class)
~~~

~~~java
// 调用tryLock()，尝试获取一次锁
@Lock4j(key = "userId", lockTemplate = RedisTemplateLockTemplate.class)
~~~

~~~java
// 调用tryLock(timeout, TimeUtil.MILLISECONDS)，尝试在指定时间内获取一次锁
// acquireTimeout：获取锁的超时时间
@Lock4j(key = "userId", acquireTimeout = 2000, lockTemplate = ZookeeperLockTemplate.class)
~~~

## Spring Boot 配置

### SimplteLock

pom:

```xml
<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-lock</artifactId>
    <version>${latest.version}</version>
</dependency>
```

App class:
```java
@Resource
LockFactory lockFactory;

public User getUser() {
	LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(SimpleLockTemplate.class);
    Lock lock = lockTemplate.getLock("123");
    // 参考上述编程式代码
    // dosomething...
}
```

### RedissonLock

pom:

```xml
<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-lock-redisson</artifactId>
    <version>${latest.version}</version>
</dependency>
```

App class:

```java
@Resource
LockFactory lockFactory;

public User getUser() {
	LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(RedissonLockTemplate.class);
    Lock lock = lockTemplate.getLock("123");
    // 参考上述编程式代码
    // dosomething...
}

// 可以自定义RedissonClient
```

spring boot application.yml config:

```yaml
# redis或自定义其他属性
spring:
  redis
    host: 127.0.0.1
    port: 6379
    database: 0
#    password: 123456
    timeout: 6000
```

### RedisTemplateLock

pom:

```xml
<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-lock-redis-template</artifactId>
    <version>${latest.version}</version>
</dependency>
```

App class:

```java
@Resource
LockFactory lockFactory;

public User getUser() {
	LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(RedisTemplateLockTemplate.class);
    Lock lock = lockTemplate.getLock("123");
    // 参考上述编程式代码
    // dosomething...
}

// 可以自定义RedissonClient
```

spring boot application.yml config:

```yaml
# redis或自定义其他属性
spring:
  redis
    host: 127.0.0.1
    port: 6379
    database: 0
#    password: 123456
    timeout: 6000
```

### ZookeeperLock：

pom:

```xml
<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-lock-zookeeper</artifactId>
    <version>${latest.version}</version>
</dependency>
```

App class:

```java
@Resource
LockFactory lockFactory;

public User getUser() {
	LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(ZookeeperLockTemplate.class);
    Lock lock = lockTemplate.getLock("123");
    // 参考上述编程式代码
    // dosomething...
}

// 可以自定义RedissonClient
```

spring boot application.yml config:

```yaml
# 共用spring.clound.zookeeper
spring:
  cloud:
    zookeeper: 
      connect-string: 127.0.0.1:2181
      connection-timeout: 15000 # 必须大于15秒
      base-sleep-time-ms: 1000
      max-retries: 3
      max-sleep-ms: 10000
      session-timeout: 30000 
```

### 