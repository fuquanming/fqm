package com.fqm.framework.locks.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;
import com.fqm.framework.locks.constant.Constants;
import com.fqm.framework.locks.impl.RedisLock;

/**
 * RedisTemplate锁模板
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisLockTemplate implements LockTemplate<RedisLock> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private final StringRedisTemplate stringRedisTemplate;
    private final LockFactory lockFactory;
    
    public RedisLockTemplate(StringRedisTemplate stringRedisTemplate, LockFactory lockFactory) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockFactory = lockFactory;
    }

    @Override
    public RedisLock getLock(String key) {
        return new RedisLock(stringRedisTemplate, Constants.PREFIX_KEY + key, lockFactory);
    }
    
    @Override
    public LockMode getLockMode() {
        return LockMode.redis;
    }

    public void destory() {
        RedisLock.getHashedWheelTimer().stop();
        logger.info("RedisLock stop HashedWheelTimer");
    }
}
