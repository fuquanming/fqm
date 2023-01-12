## 消息队列

MQ 是一个 Java 消息队列抽象，它为各种消息队列解决方案提供一致的使用。

目前有七种实现：`EMQX`，`Kafka`，`RabbitMQ` ，`Redis`，`Redisson` ，`RocketMQ` ，`Zookeeper` 。

MQ 的全部功能：

* 同步发送消息 
* 异步发送消息
* 同步发送延迟消息
* 消息监听（接收消息）

要求:

* JDK 1.8
* Spring Boot 2.7.2+ (可选)

## emqx

物联网 MQTT 消息服务器，基于 emqx 实现的消息服务。

### 1、pom 引入依赖

~~~xml
<dependency>
    <groupId>io.github.fuquanming</groupId>
    <artifactId>fqm-spring-boot-starter-mq-emqx</artifactId>
    <version>1.0.4</version>
</dependency>
~~~

### 2、yml 配置





