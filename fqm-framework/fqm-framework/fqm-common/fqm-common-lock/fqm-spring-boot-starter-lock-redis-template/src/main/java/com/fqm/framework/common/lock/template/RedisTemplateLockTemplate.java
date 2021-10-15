package com.fqm.framework.common.lock.template;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.fqm.framework.common.lock.constant.Constants;
import com.fqm.framework.common.lock.impl.RedisTemplateLock;

/**
 * RedisTemplate锁模板
 * 
 * @version 
 * @author 傅泉明
 */
public class RedisTemplateLockTemplate implements LockTemplate<RedisTemplateLock> {

    private final StringRedisTemplate stringRedisTemplate;
    
    public RedisTemplateLockTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public RedisTemplateLock getLock(String key) {
        return new RedisTemplateLock(stringRedisTemplate, Constants.PREFIX_KEY + key);
    }

}
