package com.fqm.framework.cache.spring.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.fqm.framework.cache.spring.CacheManagerType;
import com.fqm.framework.cache.spring.MultilevelCacheManager;
import com.fqm.framework.common.cache.spring.builder.CacheBuilder;
import com.fqm.framework.common.cache.spring.builder.CaffeineCacheBuilders;
import com.fqm.framework.common.cache.spring.builder.RedisCacheBuilders;
import com.fqm.framework.common.redis.listener.spring.CacheRedisKeyDeleteEventHandle;
import com.fqm.framework.common.redis.listener.spring.KeyDeleteEventMessageListener;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@EnableCaching
@Configuration
public class MultilevelCacheManagerRedisAutoConfiguration extends ApplicationObjectSupport {
    
    @Bean
    @ConditionalOnMissingBean(value = RedisCacheConfiguration.class)
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                ;
    }
    
//    @Bean(name = "multilevelCacheRedis", destroyMethod = "destroy")
    @Bean(name = CacheManagerType.MULTI_LEVEL_CACHE_MANAGER_REDIS, destroyMethod = "destroy")
    @ConditionalOnMissingBean(value = MultilevelCacheManager.class)
    public MultilevelCacheManager multiLevelCacheManager(RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration redisCacheConfiguration) {
        MultilevelCacheManager cacheManager = new MultilevelCacheManager(this.getApplicationContext());
        List<CacheBuilder> cacheBuilders = new ArrayList<>();
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
    
    /**
     * 监听Redis删除key事件
     * @param listenerContainer
     * @param cacheManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = KeyDeleteEventMessageListener.class)
    KeyDeleteEventMessageListener keyDeleteEventMessageListener(RedisMessageListenerContainer listenerContainer) {
        return new KeyDeleteEventMessageListener(listenerContainer);
    }
    
    /**
     * 缓存处理 监听Redis删除key事件
     * @param listenerContainer
     * @param cacheManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = CacheRedisKeyDeleteEventHandle.class)
    CacheRedisKeyDeleteEventHandle cacheRedisKeyDeleteEventHandle(MultilevelCacheManager cacheManager) {
        return new CacheRedisKeyDeleteEventHandle(cacheManager);
    }
}
