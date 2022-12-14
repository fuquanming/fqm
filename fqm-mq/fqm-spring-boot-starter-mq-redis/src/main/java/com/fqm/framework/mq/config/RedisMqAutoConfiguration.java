package com.fqm.framework.mq.config;

import com.fqm.framework.common.core.util.system.SystemUtil;
import com.fqm.framework.common.redis.listener.spring.KeyExpiredEventMessageListener;
import com.fqm.framework.mq.MqFactory;
import com.fqm.framework.mq.MqMode;
import com.fqm.framework.mq.annotation.MqListenerAnnotationBeanPostProcessor;
import com.fqm.framework.mq.listener.MqListenerParam;
import com.fqm.framework.mq.listener.MqRedisKeyExpiredEventHandle;
import com.fqm.framework.mq.listener.RedisMqListener;
import com.fqm.framework.mq.redis.StreamInfo.InfoGroup;
import com.fqm.framework.mq.redis.StreamInfo.InfoGroups;
import com.fqm.framework.mq.scripts.LuaScriptUtil;
import com.fqm.framework.mq.tasker.RedisMqDeadMessageTasker;
import com.fqm.framework.mq.template.RedisMqTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Redis????????????????????????
 * 
 * @version 
 * @author ?????????
 */
@Configuration
@AutoConfigureAfter(MqAutoConfiguration.class)
@ConditionalOnBean(MqProperties.class)
public class RedisMqAutoConfiguration {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Bean
    @ConditionalOnMissingBean
    @Order(200)
    RedisMqTemplate redisMqTemplate(
            MqFactory mqFactory,
            StringRedisTemplate stringRedisTemplate) {
        RedisMqTemplate redisMqTemplate = new RedisMqTemplate(stringRedisTemplate);
        mqFactory.addMqTemplate(redisMqTemplate);
        return redisMqTemplate;
    }
    
    /**
     * ?????? Redis Stream ?????????????????????
     *
     * Redis Stream ??? xreadgroup ?????????https://www.geek-book.com/src/docs/redis/redis/redis.io/commands/xreadgroup.html
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            MqListenerAnnotationBeanPostProcessor mq, StringRedisTemplate stringRedisTemplate
            , MqProperties mp
            ) {
        // ??????????????????
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> containerOptions = null;
        // ??????????????????????????????????????????
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = null;
        if (mq.getListeners() != null && !mq.getListeners().isEmpty()) {
            containerOptions = StreamMessageListenerContainerOptions.builder()
                    // ????????????????????????????????????
                    .batchSize(10) 
                    .errorHandler(Throwable::printStackTrace)
                    // ????????????????????????0????????????????????????????????????????????????
                    .pollTimeout(Duration.ZERO)
                    .serializer(new StringRedisSerializer())
                    .build();
            // ??????????????????????????????????????????
            container = StreamMessageListenerContainer.create(redisConnectionFactory, containerOptions);
            
            for (MqListenerParam v : mq.getListeners()) {
                String name = v.getName();
                MqConfigurationProperties properties = mp.getMqs().get(name);
                if (properties != null && MqMode.REDIS.equalMode(properties.getBinder())) {
                    buildListener(stringRedisTemplate, container, v, properties);
                }
            }
        }
        return container;
    }

    private void buildListener(StringRedisTemplate stringRedisTemplate,
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> container, MqListenerParam v,
            MqConfigurationProperties properties) {
        // ?????? listener ????????????????????????
        boolean createGroup = true;
        // 
        /**
         * ????????????spring-boot->2.2.0.RELEASE,redisson-spring-boot-starter->3.13.1??????????????????
         * ??????pom?????????redissonClient?????????????????????
         * <dependency>
               <groupId>org.redisson</groupId>
               <artifactId>redisson-spring-boot-starter</artifactId>
           </dependency>
         * 
         */
        /** spring-boot->2.2.0.RELEASE,redisson-spring-boot-starter->3.13.1?????????????????? */
        /** spring-boot->2.2.0.RELEASE,redisson-spring-boot-starter->3.13.1?????????????????? */
        
        String group = properties.getGroup();
        String topic = properties.getTopic();
        Assert.isTrue(StringUtils.hasText(group), "Please specific [group] under mq configuration.");
        Assert.isTrue(StringUtils.hasText(topic), "Please specific [topic] under mq configuration.");
        // Lua??????????????????
        try {
            InfoGroups gs = LuaScriptUtil.getInfoGroups(topic, stringRedisTemplate);
            if (gs != null) {
                for (Iterator<InfoGroup> it = gs.iterator(); it.hasNext();) {
                    InfoGroup g = it.next();
                    if (g.groupName().equals(group)) {
                        createGroup = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("createGroup error", e);
        }
        if (createGroup) {
            try {
                // ??????????????????mkstream ????????????????????????topic ??????????????????????????? mkstream
                // XGROUP CREATE t2 t2 0
                LuaScriptUtil.createGroup(topic, ReadOffset.from("0"), group, stringRedisTemplate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // ?????? Consumer ??????
        Consumer consumer = Consumer.from(group, buildConsumerName());
        // ?????? Consumer ??????????????????????????????????????????
        StreamOffset<String> streamOffset = StreamOffset.create(topic, ReadOffset.lastConsumed());
        // ?????? Consumer ??????
        StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                .builder(streamOffset).consumer(consumer)
                // ????????? ack
                .autoAcknowledge(false)
                // ????????????????????????????????????????????????????????????????????????????????????????????? false
                .cancelOnError(throwable -> false) 
                ;
        container.register(builder.build(), new RedisMqListener(v.getBean(), v.getMethod(), stringRedisTemplate, topic, group));
        logger.info("Init RedisMqListener,bean={},method={},topic={},group={}", v.getBean().getClass(), v.getMethod().getName(), topic, group);
    }

    /**
     * redis??????????????????????????????????????????????????????
     * @param mq
     * @param stringRedisTemplate
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    RedisMqDeadMessageTasker redisMqDeadMessageTasker(MqListenerAnnotationBeanPostProcessor mq, StringRedisTemplate stringRedisTemplate
            , MqProperties mp) {
        Set<String> topics = new HashSet<>();
        for (MqListenerParam v : mq.getListeners()) {
            String name = v.getName();
            MqConfigurationProperties properties = mp.getMqs().get(name);
            if (properties != null && MqMode.REDIS.equalMode(properties.getBinder())) {
                topics.add(properties.getTopic());
            }
        }
        
        return new RedisMqDeadMessageTasker(stringRedisTemplate, topics, 1, 60);
    }
    
    /**
     * ???????????????????????????????????? IP + ????????????????????????
     *
     * @return ???????????????
     */
    private static String buildConsumerName() {
        return String.format("%s@%d", SystemUtil.getHostAddress(), SystemUtil.getCurrentPid());
    }

    @Bean
    @ConditionalOnMissingBean(value = RedisMessageListenerContainer.class)
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
    
    /**
     * ??????Redis??????key??????
     * @param listenerContainer
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = KeyExpiredEventMessageListener.class)
    KeyExpiredEventMessageListener keyExpiredEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        return new KeyExpiredEventMessageListener(listenerContainer);
    }
    
    /**
     * ?????????????????? ??????Redis??????key??????
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = MqRedisKeyExpiredEventHandle.class)
    MqRedisKeyExpiredEventHandle mqRedisKeyExpiredEventHandle(StringRedisTemplate stringRedisTemplate) {
        return new MqRedisKeyExpiredEventHandle(stringRedisTemplate);
    }
    
}
