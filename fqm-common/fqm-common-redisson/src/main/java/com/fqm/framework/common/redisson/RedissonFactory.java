package com.fqm.framework.common.redisson;

import java.util.ArrayList;
import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
/**
 * 
 * 生成RedissonClient
 * @version 
 * @author 傅泉明
 */
public class RedissonFactory {
    
    private RedissonFactory() {
    }

    /**
     * 返回单机或集群的客户端 
     * @return
     */
    public static RedissonClient getClient(RedissonConfig redissonProperties) {
        Config config = null;
        if (redissonProperties.getCluster() == null) {
            config = getSingleConfig(redissonProperties);
        } else {
            config = getClusterConfig(redissonProperties);
        }
        return Redisson.create(config);
    }
    
    public static Config getSingleConfig(RedissonConfig redissonProperties) {
        Config config = new Config();
        // 字符编码
        config.setCodec(JsonJacksonCodec.INSTANCE) 
        .useSingleServer().setAddress("redis://" + redissonProperties.getHost() + ":" + redissonProperties.getPort())
        .setDatabase(redissonProperties.getDatabase())
        .setUsername(redissonProperties.getUsername())
        .setPassword(redissonProperties.getPassword())
        .setSslEnableEndpointIdentification(redissonProperties.isSsl());
        return config;
    }
    
    public static Config getClusterConfig(RedissonConfig redissonProperties) {
        List<String> clusterNodes = new ArrayList<>();
        for (int i = 0; i < redissonProperties.getCluster().getNodes().size(); i++) {
            clusterNodes.add("redis://" + redissonProperties.getCluster().getNodes().get(i));
        }
        Config config = new Config();
        // 字符编码
        config.setCodec(JsonJacksonCodec.INSTANCE)
        .useClusterServers()
        .addNodeAddress(clusterNodes.toArray(new String[clusterNodes.size()]))
        .setUsername(redissonProperties.getUsername())
        .setPassword(redissonProperties.getPassword())
        .setSslEnableEndpointIdentification(redissonProperties.isSsl());
        return config;
    }
    
}
