package com.fqm.framework.common.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.lock.template.LockTemplate;
import com.fqm.framework.common.lock.template.SimpleLockTemplate;

/**
 * 锁工厂
 * 
 * @version 
 * @author 傅泉明
 */
public class LockFactory {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    private Map<String, LockTemplate<?>> lockTemplateMap = new ConcurrentHashMap<>();
    
    private SimpleLockTemplate memoryLockTemplate = new SimpleLockTemplate();
    
    public LockFactory addLockTemplate(LockTemplate<?> lockTemplate) {
        logger.info("init LockTemplate->{}", lockTemplate.getClass());
        lockTemplateMap.put(lockTemplate.getClass().getName(), lockTemplate);
        return this;
    }
    
    @SuppressWarnings("rawtypes")
    public LockTemplate<?> getLockTemplate(Class<? extends LockTemplate> lockTemplateClass) {
        if (lockTemplateClass == null || lockTemplateClass == LockTemplate.class
                || lockTemplateClass == SimpleLockTemplate.class) {
            // 获取默认lockTemplate
            return memoryLockTemplate;
        }
        return lockTemplateMap.get(lockTemplateClass.getName());
    }
    
}
