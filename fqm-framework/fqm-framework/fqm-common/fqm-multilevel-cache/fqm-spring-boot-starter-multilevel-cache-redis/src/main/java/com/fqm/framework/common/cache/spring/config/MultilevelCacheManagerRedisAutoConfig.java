package com.fqm.framework.common.cache.spring.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.fqm.framework.common.cache.spring.MultilevelCacheManager;
import com.fqm.framework.common.cache.spring.builder.CacheBuilder;
import com.fqm.framework.common.cache.spring.builder.CaffeineCacheBuilders;
import com.fqm.framework.common.cache.spring.builder.RedisCacheBuilders;
import com.fqm.framework.common.redis.listener.spring.RedisKeyDeleteListener;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class MultilevelCacheManagerRedisAutoConfig extends ApplicationObjectSupport {
    
    @Bean
    @ConditionalOnMissingBean(value = RedisCacheConfiguration.class)
    public RedisCacheConfiguration redisCacheConfiguration() {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                ;
        return defaultCacheConfig;
    }
    
    @Bean(name = "multilevelCacheRedis", destroyMethod = "destroy")
    @ConditionalOnMissingBean(value = MultilevelCacheManager.class)
    public MultilevelCacheManager multiLevelCacheManager(RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration redisCacheConfiguration) {
        MultilevelCacheManager cacheManager = new MultilevelCacheManager(this.getApplicationContext());
        List<CacheBuilder> cacheBuilders = new ArrayList<CacheBuilder>();
        cacheBuilders.add(new CaffeineCacheBuilders());
        cacheBuilders.add(new RedisCacheBuilders(redisConnectionFactory, redisCacheConfiguration));
        cacheManager.setCacheBuilders(cacheBuilders);
        return cacheManager;
    }
    
    @Bean
    @ConditionalOnMissingBean(value = RedisMessageListenerContainer.class)
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
    
    @Bean
    @ConditionalOnMissingBean(value = RedisKeyDeleteListener.class)
    RedisKeyDeleteListener redisKeyDeleteListener(RedisMessageListenerContainer listenerContainer, MultilevelCacheManager cacheManager) {
        return new RedisKeyDeleteListener(listenerContainer, cacheManager);
    }
}
