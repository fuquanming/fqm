package com.fqm.framework.common.redisson;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
/**
 * 
 * 生成 RedissonClient
 * 如果要 RedisTemplate、StringRedisTemplate，使用Redisson连接
 * 在父工程中使用 redisson-spring-boot-starter。注意版本号
 * @version 
 * @author 傅泉明
 */
public class RedissonFactory {
    
    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";
    
    private RedissonFactory() {
    }

    /**
     * 返回单机或集群的客户端 
     * @return
     */
    public static RedissonClient getClient(RedisProperties redisProperties) {
        Duration timeoutValue = redisProperties.getTimeout();
        int timeout;
        if(null == timeoutValue){
            timeout = 10000;
        } else {
            timeout = (int)timeoutValue.toMillis();
        }
        
        Config config = new Config().setCodec(JsonJacksonCodec.INSTANCE);
        // 哨兵
        if (null != redisProperties.getSentinel()) {
            String[] nodes = convert(redisProperties.getSentinel().getNodes());
            config = new Config();
            // 字符编码
            config.useSentinelServers()
                .setMasterName(redisProperties.getSentinel().getMaster())
                .addSentinelAddress(nodes)
                .setDatabase(redisProperties.getDatabase())
                .setConnectTimeout(timeout)
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword());
        } else if (null != redisProperties.getCluster()) {
            // 集群
            String[] nodes = convert(redisProperties.getCluster().getNodes());
            config.useClusterServers()
                .addNodeAddress(nodes)
                .setConnectTimeout(timeout)
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword());
        } else {
            // 单机
            String prefix = REDIS_PROTOCOL_PREFIX;
            if (redisProperties.isSsl()) {
                prefix = REDISS_PROTOCOL_PREFIX;
            }
            config.useSingleServer()
                .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setDatabase(redisProperties.getDatabase())
                .setConnectTimeout(timeout)
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }
    
    private static String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
                nodes.add(REDIS_PROTOCOL_PREFIX + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }
    
}
