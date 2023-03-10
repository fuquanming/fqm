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
    
    private Map<LockMode, LockTemplate<? extends Lock>> lockTemplateMap = new ConcurrentHashMap<>();
    
    private SimpleLockTemplate memoryLockTemplate = new SimpleLockTemplate();
    
    public LockFactory() {
        addLockTemplate(memoryLockTemplate);
    }
    
    public LockFactory addLockTemplate(LockTemplate<? extends Lock> lockTemplate) {
        logger.info("init LockTemplate->{}", lockTemplate.getClass());
        lockTemplateMap.put(lockTemplate.getLockMode(), lockTemplate);
        return this;
    }
    
    @SuppressWarnings("rawtypes")
    public LockTemplate getLockTemplate(LockMode lockMode) {
        if (null == lockMode) {
            return null;
        } else if (LockMode.SIMPLE == lockMode) {
            return memoryLockTemplate;
        }
        return lockTemplateMap.get(lockMode);
    }
    
    public boolean containsLockTemplate(LockMode lockMode) {
        return lockTemplateMap.containsKey(lockMode);
    }
    
}
