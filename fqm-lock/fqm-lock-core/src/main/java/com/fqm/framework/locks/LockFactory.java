package com.fqm.framework.locks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.locks.template.LockTemplate;
import com.fqm.framework.locks.template.SimpleLockTemplate;

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
        String lockTemplateName = lockTemplate.getClass().getName();
        lockTemplateMap.put(lockTemplateName, lockTemplate);
        lockTemplateMap.put(lockTemplate.getLockMode().name(), lockTemplate);
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
        } else if (LockMode.simple == lockMode) {
            return memoryLockTemplate;
        }
        return lockTemplateMap.get(lockMode.name());
    }
    
    public LockTemplate<?> getLockTemplate(String lockMode) {
        if (lockMode == null) {
            return null;
        } else if (LockMode.simple.name().equalsIgnoreCase(lockMode)) {
            return memoryLockTemplate;
        }
        return lockTemplateMap.get(lockMode);
    }
    
}
