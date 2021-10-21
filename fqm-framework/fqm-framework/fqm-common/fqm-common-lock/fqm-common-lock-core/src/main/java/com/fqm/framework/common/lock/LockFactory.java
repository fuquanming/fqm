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
    private Map<LockMode, LockTemplate<?>> lockModeTemplateMap = new ConcurrentHashMap<>();
    
    private SimpleLockTemplate memoryLockTemplate = new SimpleLockTemplate();
    
    public LockFactory addLockTemplate(LockTemplate<?> lockTemplate) {
        logger.info("init LockTemplate->{}", lockTemplate.getClass());
        String lockTemplateName = lockTemplate.getClass().getName();
        lockTemplateMap.put(lockTemplateName, lockTemplate);
        
        if (lockTemplateName.toUpperCase().contains(LockMode.SIMPLE.name())) {
            lockModeTemplateMap.put(LockMode.SIMPLE, lockTemplate);
        } else if (lockTemplateName.toUpperCase().contains(LockMode.REDISSON.name())) {
            lockModeTemplateMap.put(LockMode.REDISSON, lockTemplate);
        } else if (lockTemplateName.toUpperCase().contains(LockMode.REDISTEMPLATE.name())) {
            lockModeTemplateMap.put(LockMode.REDISTEMPLATE, lockTemplate);
        } else if (lockTemplateName.toUpperCase().contains(LockMode.ZOOKEEPER.name())) {
            lockModeTemplateMap.put(LockMode.ZOOKEEPER, lockTemplate);
        }
        
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
    
    public LockTemplate<?> getLockTemplate(LockMode lockMode) {
        if (lockMode == null) {
            return null;
        } else if (LockMode.SIMPLE == lockMode) {
            return memoryLockTemplate;
        }
        return lockModeTemplateMap.get(lockMode);
    }
    
}
