package com.fqm.framework.common.lock.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fqm.framework.common.lock.LockFactory;
import com.fqm.framework.common.lock.constant.Constants;
import com.fqm.framework.common.lock.impl.RedisTemplateLock;

/**
 * RedisTemplate锁模板
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisTemplateLockTemplate implements LockTemplate<RedisTemplateLock> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private final StringRedisTemplate stringRedisTemplate;
    private final LockFactory lockFactory;
    
    public RedisTemplateLockTemplate(StringRedisTemplate stringRedisTemplate, LockFactory lockFactory) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockFactory = lockFactory;
    }

    @Override
    public RedisTemplateLock getLock(String key) {
        return new RedisTemplateLock(stringRedisTemplate, Constants.PREFIX_KEY + key, lockFactory);
    }

    void destory() {
        RedisTemplateLock.getHashedWheelTimer().stop();
        logger.info("RedisTemplateLock stop HashedWheelTimer");
    }
}
