## 消息队列

MQ 是一个 Java 消息队列抽象，它为各种消息队列解决方案提供一致的使用。

目前有七种实现：`EMQX`，`Kafka`，`RabbitMQ` ，`Redis`，`Redisson` ，`RocketMQ` ，`Zookeeper` 。

MQ 的全部功能：

* 发送同步消息 
* 发送异步消息
* 发送同步延迟消息
* 监听消息（接收消息）
* 死信队列

要求：

* JDK 1.8+
* Spring Boot 2.4.2+ (可选)

## 版本

| 消息组件名称                         | 版本号 | 说明                       |
| ------------------------------------ | ------ | -------------------------- |
| fqm-spring-boot-starter-mq-emqx      | 1.0.5  |                            |
| fqm-spring-boot-starter-mq-kafka     | 1.0.5  | 不支持【发送同步延迟消息】 |
| fqm-spring-boot-starter-mq-rabbit    | 1.0.5  |                            |
| fqm-spring-boot-starter-mq-redis     | 1.0.5  | 不支持【发送异步消息】     |
| fqm-spring-boot-starter-mq-redisson  | 1.0.5  |                            |
| fqm-spring-boot-starter-mq-rocket    | 1.0.5  |                            |
| fqm-spring-boot-starter-mq-zookeeper | 1.0.5  | 不支持【发送异步消息】     |

## 快速开始

### 1、pom 引入依赖

`fqm-spring-boot-starter-mq-xxx` ：`xxx` 为 [版本](#版本) 中的消息组件名称

`latest.version`：为 [版本](#版本) 中的版本号

~~~xml
<dependency>
    <groupId>io.github.fuquanming</groupId>
    <artifactId>fqm-spring-boot-starter-mq-xxx</artifactId>
    <version>{latest.version}</version>
</dependency>
~~~

### 2、yml 配置

连接MQ服务端配置参考文档后面的配置：[emqx](#emqx)、[kafka](#kafka)、[rabbit](#rabbit)、[redis](#redis)、[redisson](#redisson)、[rocket](#rocket)、[zookeeper](#zookeeper)

~~~yaml
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	emqx-topic:                             # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: emqx-topic                     # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: emqx   						# 消息组件，参考 @MqMode
    emqx-topic-dead:                        # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: emqx-topic.DLQ                 # 死信主题：topic + ".DLQ" 
      group: my-topic.DLQ-zookeeper			# 消费者组，使用 @MqListener时，必填
      binder: emqx						    # 消息组件，参考 @MqMode
~~~

### 3、发送消息 & 监听消息

#### 消息内容：User 对象

~~~java
package com.fqm.test.model;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Integer age;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

}
~~~

#### 构建 User 对象

~~~java
package com.fqm.test.controller;

import java.util.concurrent.atomic.AtomicInteger;

import com.fqm.test.model.User;

public class BaseController {

    AtomicInteger atomicInteger = new AtomicInteger();
    
    public User getUser() {
        User user = new User();
        user.setAge(atomicInteger.incrementAndGet());
        user.setName("张三");
        return user;
    }    
}
~~~

#### 示例

~~~java
package com.fqm.test.mq.controller;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.annotation.MqListener;
import com.fqm.framework.mq.client.producer.SendCallback;
import com.fqm.framework.mq.client.producer.SendResult;
import com.fqm.framework.mq.config.MqProducer;
import com.fqm.test.controller.BaseController;
import com.fqm.test.model.User;

@RestController
public class EmqxMqController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    MqFactory mqFactory;
    /** 业务名称：对应配置文件 mq.mqs.xxx */
    public static final String BUSINESS_NAME = "emqx-topic";
    /** 死信业务名称：对应配置文件 mq.mqs.xxx，死信主题：topic + ".DLQ" */
    public static final String BUSINESS_NAME_DEAD = BUSINESS_NAME + "-dead";
    @Resource
    MqProducer mqProducer;
    
    @MqListener(name = BUSINESS_NAME)
    public void receiveMessage1(String message) {
        // message 为 json 格式数据
        logger.info("receiveMessage---emqx---1={}", message);
        // 抛出异常会将消息存储到死信队列中，可以再监听死信队列，消费消息
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }
    
    @MqListener(name = BUSINESS_NAME)
    public void receiveMessage1(User user) {
        // user 对象为之前消息发送时的对象。
        logger.info("receiveMessage---emqx---11={}", user);
//        if (true) {
//            throw new RuntimeException("error 111");
//        }
    }
    
    @MqListener(name = BUSINESS_NAME_DEAD)
    public void mqDLQ(String message) {
        // 监听死信队列
        logger.info("emqx.DLQ={}", message);
    }

    @GetMapping("/mq/emqx/sendMessage")
    public Object sendEmqxMessage() {
        User user = getUser();
        try {
            // 发送同步消息
            boolean flag = mqProducer.getProducer(BUSINESS_NAME).syncSend(user);
            logger.info("emqx.send->{}", flag);
            // 发送异步消息
            mqProducer.getProducer(BUSINESS_NAME_1).asyncSend(user, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("onSuccess");
                }
                @Override
                public void onException(Throwable e) {
                    System.out.println("onException");
                }
            });
            // 通过消息模板发送消息
//            mqFactory.getMqTemplate(mqProducer.getBinder(BUSINESS_NAME)).syncSend(mqProducer.getTopic(BUSINESS_NAME), user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
    
    @GetMapping("/mq/emqx/sendDelayMessage")
    public Object sendEmqxDelayMessage() {
        User user = getUser();
        try {
            // 发送同步延迟消息
            boolean flag = mqProducer.getProducer(BUSINESS_NAME).syncDelaySend(user, 3, TimeUnit.SECONDS);
            logger.info("emqx.sendDelay->{}", flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
    
}
~~~

## emqx

物联网 MQTT，基于 MqttClient 实现。

功能：

- [x] 发送同步消息
- [x] 发送异步消息
- [x] 发送同步延迟消息
- [x] 监听消息（接收消息）
- [x] 死信队列

### yml 配置

~~~yaml
# EMQX 服务端配置
emqx:
  connect-string: tcp://127.0.0.1:1883
  username: admin
  password: admin
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	emqx-topic:                             # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: emqx-topic                     # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: emqx   						# 消息组件，参考 @MqMode
    emqx-topic-dead:                        # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: emqx-topic.DLQ                 # 死信主题：topic + ".DLQ" 
      group: my-topic.DLQ-zookeeper			# 消费者组，使用 @MqListener时，必填
      binder: emqx	
~~~

## kafka

基于 KafkaTemplate 实现

功能：

- [x] 发送同步消息
- [x] 发送异步消息
- [ ] 发送同步延迟消息
- [x] 监听消息（接收消息）
- [x] 死信队列

### yml 配置

~~~yaml
# MQ-kafka
spring:
  kafka:
    bootstrap-servers:
    - 127.0.0.1:9092
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	kafka-topic:                            # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: kafka-topic                    # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: kafka   						# 消息组件，参考 @MqMode
    kafka-topic-dead:                       # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: kafka-topic.DLT                # 死信主题：topic + ".DLQ" 
      group: my-topic.DLT-kafka			    # 消费者组，使用 @MqListener时，必填
      binder: kafka	
~~~

## rabbit

基于 RabbitTemplate 实现

功能：

- [x] 发送同步消息
- [x] 发送异步消息
- [x] 发送同步延迟消息
- [x] 监听消息（接收消息）
- [x] 死信队列

### yml 配置

~~~yaml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
    virtual-host: /
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	rabbit-topic:                           # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: rabbit-topic                   # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: rabbit   						# 消息组件，参考 @MqMode
    rabbit-topic-dead:                      # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: rabbit-topic.DLT               # 死信主题：topic + ".DLQ" 
      group: my-topic.DLQ-rabbit			# 消费者组，使用 @MqListener时，必填
      binder: rabbit	
~~~

## redis

基于 RedisTemplate -> Redis Stream 实现

功能：

- [x] 发送同步消息
- [ ] 发送异步消息
- [x] 发送同步延迟消息
- [x] 监听消息（接收消息）
- [x] 死信队列

### yml 配置

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
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	redis-topic:                            # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: redis-topic                    # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: redis   						# 消息组件，参考 @MqMode
    redis-topic-dead:                       # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: msg-group.DLT                  # 死信主题：group + ".DLQ"，监听的Topic不能有符号：%
      group: msg-group.DLQ-redis			# 消费者组，使用 @MqListener时，必填
      binder: redis	
~~~

## redisson

基于 Redisson -> DelayedQueue 实现

功能：

- [x] 发送同步消息
- [x] 发送异步消息
- [x] 发送同步延迟消息
- [x] 监听消息（接收消息）
- [x] 死信队列

### yml 配置

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
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	redisson-topic:                         # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: redisson-topic                 # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: redisson   					# 消息组件，参考 @MqMode
    redisson-topic-dead:                    # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: redisson-topic.DLQ             # 死信主题：topic + ".DLQ"，监听的Topic不能有符号：%
      group: my-topic-redisson.DLQ-redis	# 消费者组，使用 @MqListener时，必填
      binder: redisson	
~~~

## rocket

基于 RocketMQTemplate 实现

功能：

- [x] 发送同步消息
- [x] 发送异步消息
- [x] 发送同步延迟消息
- [x] 监听消息（接收消息）
- [x] 死信队列

### yml 配置

~~~yaml
rocketmq:
  name-server: http://127.0.0.1:9876 #rocketmq服务地址
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	rocket-topic:                           # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: rocket-topic                   # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: rocket   					    # 消息组件，参考 @MqMode
    rocket-topic-dead:                      # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: "%DLQ%msg-group"               # 死信主题：%DLQ% + 消费组，死信主题需要在rocket控制台：主题->勾选死信->点击“TOPIC配置”->修改 perm 值，修改为6（可读可写权限）
      group: '%DLQ%msg-group-DLQ-rocket'	# 正则表达式：^[%|a-zA-Z0-9_-]+$，不能和原topic的消费者组一样，一样消费会不及时，不全和原topic数据冲突
      binder: rocket	
~~~

## zookeeper

基于 curator-recipes -> DistributedDelayQueue 实现

功能：

- [x] 发送同步消息
- [ ] 发送异步消息
- [x] 发送同步延迟消息
- [x] 监听消息（接收消息）
- [x] 死信队列

### yml 配置

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
# 通用消息队列配置
mq:
  enabled: true								# 开启消息队列
  mqs:
	zookeeper-topic:                        # 业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: zookeeper-topic                # 消息主题
      group: msg-group						# 消费者组，使用 @MqListener时，必填
      binder: zookeeper   					# 消息组件，参考 @MqMode
    zookeeper-topic-dead:                   # 死信业务名称(唯一)，用 "[]" 处理特殊字符，如. %
      topic: zookeeper-topic.DLQ            # 死信主题：topic + ".DLQ"
      group: my-topic.DLQ-zookeeper	        # 消费者组，使用 @MqListener时，必填
      binder: zookeeper	
~~~

