package com.fqm.framework.locks.config;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.fqm.framework.common.zookeeper.ZookeeperProperties;
import com.fqm.framework.common.zookeeper.ZookeeperFactory;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.template.ZookeeperLockTemplate;

/**
 * Redisson 锁自动装配
 * 使用spring.cloud.zookeeper配置文件
 * zookeeper建立连接采用异步操作，连接操作后，并不能保证zk连接已成功。
 * connectionTimeout 默认15秒，加大该值
 * 如果zookeeper连接成功之前访问zookeeper，会出现错误：
 * org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class ZookeeperLockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Order(300)
    public ZookeeperLockTemplate zookeeperLockTemplate(LockFactory lockFactory, CuratorFramework curatorFramework) {
        ZookeeperLockTemplate zookeeperLockTemplate = new ZookeeperLockTemplate(curatorFramework);
        lockFactory.addLockTemplate(zookeeperLockTemplate);
        return zookeeperLockTemplate;
    }
    /**
     * 使用spring-cloud-starter-zookeeper 初始化 CuratorFramework，不用初始化ZookeeperConfig及CuratorFramework，
     * 会自动初始化CuratorFramework，使用spring.cloud.zookeeper
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.cloud.zookeeper")
    public ZookeeperProperties zookeeperProperties() {
        return new ZookeeperProperties();
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnMissingBean(CuratorFramework.class)
    @Order(200)
    public CuratorFramework curatorFramework(ZookeeperProperties zookeeperProperties) {
        return ZookeeperFactory.buildCuratorFramework(zookeeperProperties);
    }

}
