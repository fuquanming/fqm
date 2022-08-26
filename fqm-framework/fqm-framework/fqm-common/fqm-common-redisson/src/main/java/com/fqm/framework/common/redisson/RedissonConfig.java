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
public class RedissonConfig {

    private RedissonProperties redissonProperties;
    
    public RedissonConfig(RedissonProperties redissonProperties) {
        this.redissonProperties = redissonProperties;
    }
    
    /**
     * 返回单机或集群的客户端 
     * @return
     */
    public RedissonClient getClient() {
        Config config = null;
        if (redissonProperties.getCluster() == null) {
            config = getSingleConfig();
        } else {
            config = getClusterConfig();
        }
        return Redisson.create(config);
    }
    
    public Config getSingleConfig() {
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE) // 字符编码
        .useSingleServer().setAddress("redis://" + redissonProperties.getHost() + ":" + redissonProperties.getPort())
        .setDatabase(redissonProperties.getDatabase())
        .setUsername(redissonProperties.getUsername())
        .setPassword(redissonProperties.getPassword())
        .setSslEnableEndpointIdentification(redissonProperties.isSsl());
        return config;
    }
    
    public Config getClusterConfig() {
        List<String> clusterNodes = new ArrayList<>();
        for (int i = 0; i < redissonProperties.getCluster().getNodes().size(); i++) {
            clusterNodes.add("redis://" + redissonProperties.getCluster().getNodes().get(i));
        }
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE)// 字符编码
        .useClusterServers()
        .addNodeAddress(clusterNodes.toArray(new String[clusterNodes.size()]))
        .setUsername(redissonProperties.getUsername())
        .setPassword(redissonProperties.getPassword())
        .setSslEnableEndpointIdentification(redissonProperties.isSsl());
        return config;
    }
    
}
