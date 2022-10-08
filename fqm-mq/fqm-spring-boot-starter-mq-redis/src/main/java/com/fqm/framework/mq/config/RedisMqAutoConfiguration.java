package com.fqm.framework.mq.config;

import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
//import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup;
//import org.springframework.data.redis.connection.stream.StreamRecords;
//import org.springframework.data.redis.connection.stream.StringRecord;
//import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup;
//import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.util.ErrorHandler;

import com.fqm.framework.common.core.util.StringUtil;
import com.fqm.framework.common.core.util.system.SystemUtil;
import com.fqm.framework.common.redis.listener.spring.KeyExpiredEventMessageListener;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.MqRedisKeyExpiredEventHandle;
import com.fqm.framework.mq.listener.RedisMqListener;
import com.fqm.framework.mq.redis.StreamInfo.XInfoGroup;
import com.fqm.framework.mq.redis.StreamInfo.XInfoGroups;
import com.fqm.framework.mq.scripts.LuaScriptUtil;
import com.fqm.framework.mq.tasker.RedisMqDeadMessageTasker;
import com.fqm.framework.mq.template.RedisMqTemplate;
import com.google.common.base.Preconditions;

/**
 * Redis消息队列自动装配
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class RedisMqAutoConfiguration {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
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
            , MqProperties mp
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
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties == null) {
                // 遍历mp.mqs
                for (MqConfigurationProperties mcp : mp.getMqs().values()) {
                    if (mcp.getName().equals(name) && MqMode.redis.name().equals(mcp.getBinder())) {
                        properties = mcp;
                        break;
                    }
                }

            }
            if (properties != null && MqMode.redis.name().equals(properties.getBinder())) {
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
//                try {
//                    org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups gs = stringRedisTemplate.opsForStream().groups(v.getDestination());
//                    if (gs != null) {
//                        for (Iterator<org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup> it = gs.iterator(); it.hasNext();) {
//                            org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroup g = it.next();
//                            if (g.groupName().equals(v.getGroup())) {
//                                createGroup = false;// 
//                                break;
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    createGroup = true;
//                }
                /** spring-boot->2.4.2,redisson-spring-boot-starter->3.15.1，使用新API */
                String group = properties.getGroup();
                String topic = properties.getTopic();
                Preconditions.checkArgument(StringUtil.isNotBlank(group), "Please specific [group] under mq configuration.");
                Preconditions.checkArgument(StringUtil.isNotBlank(topic), "Please specific [topic] under mq configuration.");
                // Lua获取消费者组
                try {
                    XInfoGroups gs = LuaScriptUtil.getXInfoGroups(topic, stringRedisTemplate);
                    if (gs != null) {
                        for (Iterator<XInfoGroup> it = gs.iterator(); it.hasNext();) {
                            XInfoGroup g = it.next();
                            if (g.groupName().equals(group)) {
                                createGroup = false;// 
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    createGroup = true;
                }
                
                
                if (createGroup) {
                    try {
                        // 低版本没有加mkstream 参数会报错，因为topic 不存在，必须加选项 mkstream
                        // XGROUP CREATE t2 t2 0 mkstream
//                        stringRedisTemplate.opsForStream().createGroup(v.getDestination(), ReadOffset.from("0"), v.getGroup());
                        LuaScriptUtil.createGroup(topic, ReadOffset.from("0"), group, stringRedisTemplate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // 创建 Consumer 对象
                Consumer consumer = Consumer.from(group, buildConsumerName());
                // 设置 Consumer 消费进度，以最小消费进度为准
                StreamOffset<String> streamOffset = StreamOffset.create(topic, ReadOffset.lastConsumed());
                // 设置 Consumer 监听
                StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                        .builder(streamOffset).consumer(consumer)
                        .autoAcknowledge(false) // 不自动 ack
                        .cancelOnError(throwable -> false) // 默认配置，发生异常就取消消费，显然不符合预期；因此，我们设置为 false
                        ;
                container.register(builder.build(), new RedisMqListener(v.getBean(), v.getMethod(), stringRedisTemplate, topic, group));
                logger.info("Init RedisMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
            }
        }
        return container;
    }
    
    /**
     * redis消息队列，未消费成功入死信队列的任务
     * @param mq
     * @param stringRedisTemplate
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RedisMqDeadMessageTasker redisMqDeadMessageTasker(MqListenerAnnotationBeanPostProcessor mq, StringRedisTemplate stringRedisTemplate
            , MqProperties mp) {
        Set<String> topics = new HashSet<>();
        for (MqListenerParam v : mq.getListeners()) {
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties == null) {
                // 遍历mp.mqs
                for (MqConfigurationProperties mcp : mp.getMqs().values()) {
                    if (mcp.getName().equals(name) && MqMode.redis.name().equals(mcp.getBinder())) {
                        properties = mcp;
                        topics.add(properties.getTopic());
                        break;
                    }
                }
            }
        }
        
        return new RedisMqDeadMessageTasker(stringRedisTemplate, topics, 1, 60);
    }
    
    /**
     * 构建消费者名字，使用本地 IP + 进程编号的方式。
     *
     * @return 消费者名字
     */
    private static String buildConsumerName() {
//        return "consumer-1";
        return String.format("%s@%d", SystemUtil.getHostAddress(), SystemUtil.getCurrentPID());
    }

    @Bean
    @ConditionalOnMissingBean(value = RedisMessageListenerContainer.class)
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
    
    /**
     * 监听Redis过期key事件
     * @param listenerContainer
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = KeyExpiredEventMessageListener.class)
    KeyExpiredEventMessageListener keyExpiredEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        return new KeyExpiredEventMessageListener(listenerContainer);
    }
    
    /**
     * 延迟消息处理 监听Redis过期key事件
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = MqRedisKeyExpiredEventHandle.class)
    MqRedisKeyExpiredEventHandle mqRedisKeyExpiredEventHandle(StringRedisTemplate stringRedisTemplate) {
        return new MqRedisKeyExpiredEventHandle(stringRedisTemplate);
    }
    
}
