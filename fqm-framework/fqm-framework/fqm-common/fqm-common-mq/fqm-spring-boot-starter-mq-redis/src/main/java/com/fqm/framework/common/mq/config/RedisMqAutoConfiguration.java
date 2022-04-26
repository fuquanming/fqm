package com.fqm.framework.common.mq.config;

import java.time.Duration;
import java.util.Iterator;

//import org.redisson.api.RStream;
//import org.redisson.api.RedissonClient;
//import org.redisson.api.StreamGroup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup;
//import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.connection.stream.StreamOffset;
//import org.springframework.data.redis.connection.stream.StreamRecords;
//import org.springframework.data.redis.connection.stream.StringRecord;
//import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.util.ErrorHandler;

import com.fqm.framework.common.mq.MqFactory;
import com.fqm.framework.common.mq.MqMode;
import com.fqm.framework.common.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.common.mq.listener.MqListenerParam;
import com.fqm.framework.common.mq.listener.RedisMqListener;
import com.fqm.framework.common.mq.template.RedisMqTemplate;

import cn.hutool.system.SystemUtil;

/**
 * Redis消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class RedisMqAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    public RedisMqTemplate redisMqTemplate(
            MqFactory mqFactory,
            StringRedisTemplate stringRedisTemplate) {
        RedisMqTemplate redisMqTemplate = new RedisMqTemplate(stringRedisTemplate);
        mqFactory.addMqTemplate(redisMqTemplate);
        return redisMqTemplate;
    }
    
    /**
     * 创建 Redis Stream 集群消费的容器
     *
     * Redis Stream 的 xreadgroup 命令：https://www.geek-book.com/src/docs/redis/redis/redis.io/commands/xreadgroup.html
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            MqListenerAnnotationBeanPostProcessor mq, StringRedisTemplate stringRedisTemplate
//            ,RedissonClient redissonClient
            ) {
        // 创建配置对象
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> containerOptions = null;
        // 根据配置对象创建监听容器对象
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = null;
        if (mq.getListeners() != null && !mq.getListeners().isEmpty()) {
            containerOptions = StreamMessageListenerContainerOptions.builder()
                    .batchSize(10) // 一次性最多拉取多少条消息
                    .errorHandler(new ErrorHandler() {
                        @Override
                        public void handleError(Throwable t) {
                            t.printStackTrace();
                        }
                    })
                    .pollTimeout(Duration.ZERO) // 超时时间，设置为0，表示不超时（超时后会抛出异常）
                    .serializer(new StringRedisSerializer())
                    .build();
            // 根据配置对象创建监听容器对象
            container = StreamMessageListenerContainer.create(redisConnectionFactory, containerOptions);
        }
        
        for (MqListenerParam v : mq.getListeners()) {
            if (MqMode.redis.name().equals(v.getBinder())) {
                // 创建 listener 对应的消费者分组
                boolean createGroup = true;
                // 
                /**
                 * 低版本：spring-boot->2.2.0.RELEASE,redisson-spring-boot-starter->3.13.1，初始化数据
                 * 添加pom，使用redissonClient来初始化数据。
                 * <dependency>
                       <groupId>org.redisson</groupId>
                       <artifactId>redisson-spring-boot-starter</artifactId>
                   </dependency>
                 * 
                 */
                /** spring-boot->2.2.0.RELEASE,redisson-spring-boot-starter->3.13.1，初始化数据 */
//                RStream<String, String> stream = redissonClient.getStream(v.getDestination());
//                if (!stream.isExists()) {
//                    StringRecord stringRecord = StreamRecords.string(
//                            Collections.singletonMap("test", "testData")).withStreamKey(v.getDestination());
//                    RecordId recordId = stringRedisTemplate.opsForStream().add(stringRecord);
//                    stringRedisTemplate.opsForStream().delete(v.getDestination(), recordId);// 删除测试消息
//                }
//                List<StreamGroup> groups = stream.listGroups();
//                if (groups != null) {
//                    for (StreamGroup group : groups) {
//                        if (group.getName().equals(v.getGroup())) {
//                            createGroup = false;
//                            break;
//                        }
//                    }
//                }
                /** spring-boot->2.2.0.RELEASE,redisson-spring-boot-starter->3.13.1，初始化数据 */
                
                /** spring-boot->2.4.2,redisson-spring-boot-starter->3.15.1，使用新API */
                try {
                    XInfoGroups gs = stringRedisTemplate.opsForStream().groups(v.getDestination());
                    if (gs != null) {
                        for (Iterator<XInfoGroup> it = gs.iterator(); it.hasNext();) {
                            XInfoGroup g = it.next();
                            if (g.groupName().equals(v.getGroup())) {
                                createGroup = false;// 
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    createGroup = true;
                }
                /** spring-boot->2.4.2,redisson-spring-boot-starter->3.15.1，使用新API */
                
                if (createGroup) {
                    try {
                        stringRedisTemplate.opsForStream().createGroup(v.getDestination(), ReadOffset.from("0"), v.getGroup());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // 创建 Consumer 对象
                Consumer consumer = Consumer.from(v.getGroup(), buildConsumerName());
                // 设置 Consumer 消费进度，以最小消费进度为准
                StreamOffset<String> streamOffset = StreamOffset.create(v.getDestination(), ReadOffset.lastConsumed());
                // 设置 Consumer 监听
                StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                        .builder(streamOffset).consumer(consumer)
                        .autoAcknowledge(false) // 不自动 ack
                        .cancelOnError(throwable -> false) // 默认配置，发生异常就取消消费，显然不符合预期；因此，我们设置为 false
                        ;
                container.register(builder.build(), new RedisMqListener(v.getBean(), v.getMethod(), stringRedisTemplate, v.getDestination(), v.getGroup()));
            }
        }
        return container;
    }

    /**
     * 构建消费者名字，使用本地 IP + 进程编号的方式。
     *
     * @return 消费者名字
     */
    private static String buildConsumerName() {
//        return "consumer-1";
        return String.format("%s@%d", SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
    }
    
}
