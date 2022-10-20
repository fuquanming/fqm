package com.fqm.framework.locks.template;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import com.fqm.framework.locks.LockMode;
import com.fqm.framework.locks.impl.ZookeeperLock;

/**
 * Zookeeper锁模板
 * 
 * @version 
 * @author 傅泉明
 */
public class ZookeeperLockTemplate implements LockTemplate<ZookeeperLock> {

    private final CuratorFramework curatorFramework;
    
    public ZookeeperLockTemplate(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @Override
    public ZookeeperLock getLock(String key) {
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, String.format("/curator/lock4j/%s", key));
        return new ZookeeperLock(mutex);
    }
    
    @Override
    public LockMode getLockMode() {
        return LockMode.ZOOKEEPER;
    }

}
