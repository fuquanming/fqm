# 简介
要求:
* JDK1.8
* Spring Framework4.0.8+ (可选)
* Spring Boot1.1.9+ (可选)

# 入门

sentry是一个基于Django构建的现代化的实时事件日志监控、记录和聚合平台，主要用于如何快速的发现故障。

安装sentry 或者 使用sentry.io (https://sentry.io/organizations/fqm) 创建项目，并记录dsn。

~~~xml
<dependency>
    <groupId>com.fqm</groupId>
    <artifactId>fqm-spring-boot-starter-sentry</artifactId>
    <version>{latest.version}</version>
</dependency>
~~~

application.yml

~~~yaml
sentry:
  enabled: true
  dsn: https://6034ce12b9504ee39f0fc0e37b2e545f@o1026150.ingest.sentry.io/5992542
~~~

logback.xml

~~~xml
<!-- 实时事件的日志聚合平台,将error级别推送到sentry -->
<appender name="SENTRY" class="io.sentry.logback.SentryAppender">
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>ERROR</level>
    </filter>
</appender>

<!--日志打印的包的范围，及分类日志文件存储 -->
<logger name="com.fqm.test" additivity="false">
    <level value="DEBUG" />
    <appender-ref ref="STDOUT"/>
    <appender-ref ref="INFO" />
    <appender-ref ref="SENTRY" /><!-- 添加sentry日志 -->
</logger>
~~~





