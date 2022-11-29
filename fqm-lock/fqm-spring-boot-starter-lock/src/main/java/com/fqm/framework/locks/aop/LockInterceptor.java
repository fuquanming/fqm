package com.fqm.framework.locks.aop;

import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.fqm.framework.common.spring.util.ValueUtil;
import com.fqm.framework.locks.Lock;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;
import com.fqm.framework.locks.annotation.Lock4j;
import com.fqm.framework.locks.template.LockTemplate;

/**
 * 分布式锁aop处理器
 * 
 * @version 
 * @author 傅泉明
 */
public class LockInterceptor implements MethodInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final LockFactory lockFactory;
    
    private ApplicationContext applicationContext;
    
    public LockInterceptor(LockFactory lockFactory, ApplicationContext applicationContext) {
        this.lockFactory = lockFactory;
        this.applicationContext = applicationContext;
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //fix 使用其他aop组件时,aop切了两次.
        Class<?> cls = AopProxyUtils.ultimateTargetClass(invocation.getThis());
        if (!cls.equals(invocation.getThis().getClass())) {
            return invocation.proceed();
        }
        Lock4j lock4j = null;
        // 是否调用tryLock
        boolean tryLockFlag = false;
        // 调用tryLock是否成功
        boolean tryLockStatus = false;
        
        Lock lock = null;
        // 是否调用lock
        boolean block = false;
        
        String lockName = null;
        // 锁的key
        String key = null;
        
        try {
            ConfigurableBeanFactory factory = (ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory();
            
            lock4j = invocation.getMethod().getAnnotation(Lock4j.class);
            // 是否调用lock
            block = lock4j.block();
            // 锁的方式
            String lockModeStr = lock4j.lockMode();
            
            lockModeStr = ValueUtil.resolveExpression(factory, lockModeStr).toString();
            
            LockMode lockMode = LockMode.getMode(lockModeStr);
            
            LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(lockMode);
            
            if (lockTemplate == null) {
                lockTemplate = lockFactory.getLockTemplate(lock4j.lockTemplate());
            }
            // 获取锁超时时间
            long acquireTimeout = lock4j.acquireTimeout();
            // 锁的key
            key = lock4j.key();
            
            key = ValueUtil.resolveExpression(factory, key).toString();
            
            Assert.hasText(key, "lock key must be not empty");
            
            lock = lockTemplate.getLock(key);
            
            lockName = lock.getClass().getSimpleName();
            
            if (block) {
                // lock
                logger.info("lock()->{},{}", lockName, key);
                lock.lock();
                tryLockStatus = true;
                return invocation.proceed();
            } else {
                // tryLock
                tryLockFlag = true;
                if (acquireTimeout <= 0) {
                    logger.info("tryLock()->{},{}", lockName, key);
                    tryLockStatus = lock.tryLock();
                } else {
                    logger.info("tryLock({})->{},{}", lock4j.acquireTimeout(), lockName, key);
                    tryLockStatus = lock.tryLock(lock4j.acquireTimeout(), TimeUnit.MILLISECONDS);
                }
                if (tryLockStatus) {
                    return invocation.proceed();
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (lock != null && block) {
                boolean flag = lock.unlock();
                logger.info("lock->unlock()->{},{},{}", lockName, key, flag);
            }
            if (lock != null && tryLockFlag && tryLockStatus) {
                boolean flag = lock.unlock();
                logger.info("tryLock->unlock()->{},{},{}", lockName, key, flag);
            }
        }
    }

}
