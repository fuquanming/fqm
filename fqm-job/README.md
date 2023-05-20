# 任务调度
Job 是一个Java分布式任务调度抽象。提供 @JobListener 注释。

目前有2种实现：`xxl-job`，`elastic-job`

Job 的全部功能：

* 定时调度 
* 分片任务

要求:

* JDK1.8
* Spring Framework4.0.8+ (可选)
* Spring Boot1.1.9+ (可选)

# 版本

| 锁组件名称                          | 版本号 | 说明                              |
| ----------------------------------- | ------ | --------------------------------- |
| fqm-spring-boot-starter-job-xxl     | 1.0.5  | xxl-job-admin 服务端版本号:2.4.0  |
| fqm-spring-boot-starter-job-elastic | 1.0.5  | elasticjob-lite-core 版本号:3.0.3 |

| 锁组件名称                          | 版本号 | 说明                              |
| ----------------------------------- | ------ | --------------------------------- |
| fqm-spring-boot-starter-job-xxl     | 1.0.4  | xxl-job-admin 服务端版本号:2.3.0  |
| fqm-spring-boot-starter-job-elastic | 1.0.4  | elasticjob-lite-core 版本号:3.0.2 |

# 快速开始

## 1、pom 引入依赖

`fqm-spring-boot-starter-job-xxx` ：`xxx` 为 [版本](#版本) 中的消息组件名称

`latest.version`：为 [版本](#版本) 中的版本号

~~~xml
<dependency>
    <groupId>io.github.fuquanming</groupId>
    <artifactId>fqm-spring-boot-starter-job-xxx</artifactId>
    <version>{latest.version}</version>
</dependency>
~~~

## 2、yml 配置

连接服务端配置参考文档后面的配置：[xxl-job](#xxl-job)、[elastic-job](#elastic-job)

~~~yaml
## 通用任务配置，同任务组件下的任务名不能重复
job:
  enabled: true             # 开启任务
  verify: true              # 校验加载的任务组件，未校验时则配置错误将导致运行时错误
  binder: xxljob            # 执行任务组件，参考 @JobMode，统一设置任务组件
  jobs:
    xjob:                   # 任务名称，唯一，该值等于自定义注解JobListener.name()
      binder: xxljob        # 执行任务组件
      cron: 			    # xxljob非必填，由控台配置调度周期及是否启动
~~~

## 3、注解式

~~~java
package com.fqm.test.job.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.job.annotation.JobListener;
import com.fqm.framework.job.core.JobContext;

@RestController
public class XxlJobController {

    public Logger logger = LoggerFactory.getLogger(getClass());
    /** 任务名称：对应配置文件 job.jobs.xxx */
    public static final String JOB_CREATE_ORDER = "xjob";
    
    @JobListener(name = JOB_CREATE_ORDER)
    public void xjob(JobContext jobContext) {
        logger.info("XxlJobParam=" + jobContext.getJobParam());
        // 分片参数，当前分片所在的序号，从0开始
        // 如：总共2个分片总数，分片为0则取 数据%2=0的数据处理，分片为1则取 数据%2=1的数据处理，共同完成数据处理
        int shardIndex = jobContext.getShardIndex();
        // 分片参数，当前分片总数，表示该任务总共有多少个线程执行
        int shardTotal = jobContext.getShardTotal();
        logger.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
    }
    
}
~~~

# xxl-job

功能：

- [x] 定时调度
- [x] 分片任务

## yml 配置

~~~yaml
## xxljob
xxljob:
  admin:
    addresses: http://127.0.0.1:9090/xxl-job-admin # 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；为空则关闭自动注册；
  executor:
    app-name: job1 # 执行器 AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册
    ip:  # 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯实用；地址信息用于 "执行器注册" 和 "调度中心请求并触发任务"；
    port:  # ### 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；
    log-path: logs/xxl-job # 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；
    log-retention-days: 30 # 执行器日志文件保存天数 [选填] ： 过期日志自动清理, 限制值大于等于3时生效; 否则, 如-1, 关闭自动清理功能；
  accessToken:  # 执行器通讯TOKEN [选填]：非空时启用； 
## 通用任务配置，同任务组件下的任务名不能重复
job:
  enabled: true             # 开启任务
  verify: true              # 校验加载的任务组件，未校验时则配置错误将导致运行时错误
  binder: xxljob            # 执行任务组件，参考 @JobMode，统一设置任务组件
  jobs:
    xjob:                   # 任务名称，唯一，该值等于自定义注解JobListener.name()
      binder: xxljob        # 执行任务组件
      cron: 			    # xxljob非必填，由控台配置调度周期及是否启动
~~~

## 服务端

### 新增执行器

菜单 -> 执行器管理，点击新增。

| 名称     | 值                                                  |
| -------- | --------------------------------------------------- |
| AppName  | 填写 yml 配置中 xxljob.executor.app-name 对应的名称 |
| 名称     | 自定义                                              |
| 注册方式 | 选择 `自动注册`                                     |

### 新增任务

菜单 -> 任务管理，点击新增

| 名称           | 值                                                           |      |
| -------------- | ------------------------------------------------------------ | ---- |
| **执行器**     | 选择 yml 配置中 xxljob.executor.app-name 对应的名称          |      |
| **任务描述**   | 自定义                                                       |      |
| **注册方式**   | 自定义                                                       |      |
| **调度类型**   | 选择 `CRON`                                                  |      |
| **Cron**       | 如：0/5 * * * * ? （表示每个5秒执行）                        |      |
| **运行模式**   | 选择 `BEAN`                                                  |      |
| **JobHandler** | 填写 yml 配置中 job.jobs.xjob（即xjob），xjob为需要调度的名称 |      |
| **路由策略**   | 单线程执行：选择 `第一个`，分片任务：选择 `分片广播`         |      |

### 运行

菜单 -> 任务管理，选择对应的任务，点击 `操作`  旁边的下拉箭头 -> `执行一次` 、`启动`、`修改` 等操作。

# elastic-job

功能：

- [x] 定时调度
- [x] 分片任务

## yml 配置

~~~yaml
## elasticjob 
elasticjob:
  reg-center:
    server-lists: 192.168.86.145:2181        # zookeeper地址
    namespace: elasticjob-1                 # 命名空间  
## 通用任务配置，同任务组件下的任务名不能重复
job:
  enabled: true             # 开启任务
  verify: true              # 校验加载的任务组件，未校验时则配置错误将导致运行时错误
  binder: elasticjob        # 执行任务组件，参考 @JobMode，统一设置任务组件
  jobs:
    ejob:                   # 任务名称，唯一，该值等于自定义注解JobListener.name()
      binder: elasticjob    # 执行任务组件
      cron: 0/5 * * * * ?   # 执行任务时间表达式，对应的管理控台可以修改，elasticjob必填
~~~

## 服务端

通过 elasticjob-lite-ui 控台访问

### 新增注册中心

菜单 -> 全局配置 -> 注册中心配置，点击 添加

| 名称             | 值                                                           |
| ---------------- | ------------------------------------------------------------ |
| **注册中心名称** | 自定义                                                       |
| **注册中心地址** | 填写 yml 配置中 elasticjob.reg-center.server-lists 对应的zookeeper地址 |
| **命名空间**     | 填写 yml 配置中 elasticjob.reg-center.namespace 对应的命名空间 |

添加成功后，在改页面列表中，-> 操作 -> 点击 `连接`

### 操作

菜单 -> 作用操作 -> 作业维度，选择对应的任务，点击 `操作`   -> `修改` 、`触发` 、`失效`、`终止` 等操作。

| 操作 | 说明                                                         |
| ---- | ------------------------------------------------------------ |
| 修改 | 修改配置，如 Cron 调度规则，分片总数 等                      |
| 触发 | 立即执行一次                                                 |
| 失效 | 暂停任务，可以点击 `生效` 来启动任务。如果任务所在进程重启，任务会自动启动 |
| 终止 | 任务不能再启动。如果任务所在进程重启，任务会自动启动         |