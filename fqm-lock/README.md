# 锁
lock 是一个Java分布式锁抽象。提供编程式及Lock4j注释。

目前有4种实现：

单机锁：`SimpleLock`

分布式锁：`RedissonLock`，`ZookeeperLock`，`RedisLock`

Lock 的全部功能：

* 阻塞式获取锁 
* 尝试获取一次锁
* 指定时间内获取一次锁
* 释放锁

要求:

* JDK1.8
* Spring Framework4.0.8+ (可选)
* Spring Boot1.1.9+ (可选)

# 版本

| 锁组件名称                             | 版本号 | 说明     |
| -------------------------------------- | ------ | -------- |
| fqm-spring-boot-starter-lock           | 1.0.3  | 单机锁   |
| fqm-spring-boot-starter-lock-redis     | 1.0.3  | 分布式锁 |
| fqm-spring-boot-starter-lock-redisson  | 1.0.4  | 分布式锁 |
| fqm-spring-boot-starter-lock-zookeeper | 1.0.4  | 分布式锁 |

# 快速开始

## 1、pom 引入依赖

`fqm-spring-boot-starter-lock-xxx` ：`xxx` 为 [版本](#版本) 中的消息组件名称

`latest.version`：为 [版本](#版本) 中的版本号

~~~xml
<dependency>
    <groupId>io.github.fuquanming</groupId>
    <artifactId>fqm-spring-boot-starter-lock-xxx</artifactId>
    <version>{latest.version}</version>
</dependency>
~~~

## 2、yml 配置

连接服务端配置参考文档后面的配置：[simple](#simple)、[redis](#redis)、[redisson](#redisson)、[zookeeper](#zookeeper)

~~~yaml
# 通用锁配置
lock:
  enabled: true
  verify: true                # 校验加载的锁组件，未校验时则配置错误将导致运行时错误
  binder: simple              # 锁组件，参考 @LockMode，统一设置锁方式
  locks:
    simple:                   # 业务名称，唯一
      key: user               # 锁的名称
      binder: simple          # 锁组件，参考 @LockMode，统一设置锁方式
      block: true             # 阻塞获取锁
    simple1:                   # 业务名称，唯一
      key: user               # 锁的名称
      binder: simple          # 锁组件，参考 @LockMode，统一设置锁方式
      acquire-timeout: 3000   # 获取锁的超时时间，单位：毫秒
~~~

## 3、注解式

~~~java
package com.fqm.test.lock.controller;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.locks.annotation.Lock4j;

@RestController
public class SimpleLockController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    SimpleLockUserService lockUserService;
    // 业务名称：对应配置文件 lock.locks.simple
	public static final String BUSINESS_NAME = "simple";
    
    @GetMapping("/lock4j/simple")
    public Object lock4j() {
        logger.info("Thread:{},begin", Thread.currentThread().getName());
        Object obj = null;
        try {
            obj = lockUserService.getUserByLock4jLock();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Thread:{},end,{}", Thread.currentThread().getName(), obj);
    }
}    

@Service
class SimpleLockUserService {
    @Lock4j(name = SimpleLockController.BUSINESS_NAME)
    public Object getUserByLock4jLock() {
        logger.info("Thread:{},SimpleLockUserService", Thread.currentThread().getName());
        HashMap<String, Object> user = new HashMap<>();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }
}    
~~~

## 4、编程式

~~~java
package com.fqm.test.lock.controller;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.locks.config.LockProducer;
import com.fqm.framework.locks.config.LockProducer.Lock;

@RestController
public class SimpleLockController {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    LockProducer lockProducer;
    // 业务名称：对应配置文件 lock.locks.simple1
    public static final String BUSINESS_NAME_1 = "simple1";        
    
    @GetMapping("/lock/simple")
    public Object lockCode() {
        logger.info("Thread:{},begin", Thread.currentThread().getName());
        Lock lock = null;
        boolean flag = false;
        try {
            lock = lockProducer.getLock(BUSINESS_NAME_1);
            flag = lock.lock();
            if (flag) {
                logger.info("Thread:{},lock", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (flag) {
                flag = lock.unLock();
                logger.info("Thread:{},unlock", Thread.currentThread().getName());
            }
        }                              
        return new HashMap<>();
    }
    
}
~~~

# simple

基于 ReentrantLock 实现

功能：

- [x] 阻塞式获取锁
- [x] 尝试获取一次锁
- [x] 指定时间内获取一次锁
- [x] 释放锁

## yml 配置

~~~yaml
# 通用锁配置
lock:
  enabled: true
  verify: true                # 校验加载的锁组件，未校验时则配置错误将导致运行时错误
  binder: simple              # 锁组件，参考 @LockMode，统一设置锁方式
  locks:
    simple:                   # 业务名称，唯一
      key: user               # 锁的名称
      binder: simple          # 锁组件，参考 @LockMode，统一设置锁方式
      block: true             # 阻塞获取锁
    simple1:                   # 业务名称，唯一
      key: user               # 锁的名称
      binder: simple          # 锁组件，参考 @LockMode，统一设置锁方式
      acquire-timeout: 3000   # 获取锁的超时时间，单位：毫秒
~~~

# redis

基于 RedisTemplate 实现

功能：

- [x] 阻塞式获取锁
- [x] 尝试获取一次锁
- [x] 指定时间内获取一次锁
- [x] 释放锁

## yml 配置

~~~yaml
spring:
  redis:
    host: 127.0.0.1
    port: 16379
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0
# 通用锁配置
lock:
  enabled: true
  verify: true                # 校验加载的锁组件，未校验时则配置错误将导致运行时错误
  binder: simple              # 锁组件，参考 @LockMode，统一设置锁方式
  locks:
    redis:
      key: user               # 锁的名称
      binder: redis           # 锁组件，参考 @LockMode，统一设置锁方式
      block: true             # 阻塞获取锁
    redis1:
      key: user               # 锁的名称
      binder: redis           # 锁组件，参考 @LockMode，统一设置锁方式
      acquire-timeout: 3000   # 获取锁的超时时间，单位：毫秒
~~~

# redisson

基于 RLock 实现

功能：

- [x] 阻塞式获取锁
- [x] 尝试获取一次锁
- [x] 指定时间内获取一次锁
- [x] 释放锁

## yml 配置

~~~yaml
spring:
  redis:
    host: 127.0.0.1
    port: 16379
    database: 0
# 通用锁配置
lock:
  enabled: true
  verify: true                # 校验加载的锁组件，未校验时则配置错误将导致运行时错误
  binder: simple              # 锁组件，参考 @LockMode，统一设置锁方式
  locks:
    redisson:
      key: user               # 锁的名称
      binder: redisson        # simple,redisson,redis,zookeeper
      block: true             # 阻塞获取锁
    redisson1:
      key: user               # 锁的名称
      binder: redisson        # simple,redisson,redis,zookeeper      
      acquire-timeout: 3000   # 获取锁的超时时间，单位：毫秒
~~~

# zookeeper

基于 InterProcessMutex 实现

功能：

- [x] 阻塞式获取锁
- [x] 尝试获取一次锁
- [x] 指定时间内获取一次锁
- [x] 释放锁

## yml 配置

~~~yaml
spring:
  cloud:
    zookeeper: 
      connect-string: 127.0.0.1:2181
      connection-timeout: 15000
      base-sleep-time-ms: 1000
      max-retries: 3
      max-sleep-ms: 10000
      session-timeout: 30000
# 通用锁配置
lock:
  enabled: true
  verify: true                # 校验加载的锁组件，未校验时则配置错误将导致运行时错误
  binder: simple              # 锁组件，参考 @LockMode，统一设置锁方式
  locks:
    zookeeper:
      key: user               # 锁的名称
      binder: zookeeper       # 锁组件，参考 @LockMode，统一设置锁方式
      block: true             # 阻塞获取锁
    zookeeper1:
      key: user               # 锁的名称
      binder: zookeeper       # 锁组件，参考 @LockMode，统一设置锁方式
      acquire-timeout: 3000   # 获取锁的超时时间，单位：毫秒
~~~