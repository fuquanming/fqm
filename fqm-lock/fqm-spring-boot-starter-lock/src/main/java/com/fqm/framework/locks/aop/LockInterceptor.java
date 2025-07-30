package com.fqm.framework.locks.aop;

import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.fqm.framework.common.spring.util.ValueUtil;
import com.fqm.framework.locks.Lock;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;
import com.fqm.framework.locks.annotation.Lock4j;
import com.fqm.framework.locks.config.LockConfigurationProperties;
import com.fqm.framework.locks.config.LockProducer;
import com.fqm.framework.locks.config.LockProperties;
import com.fqm.framework.locks.template.LockTemplate;

/**
 * 分布式锁aop处理器
 * 
 * @version 
 * @author 傅泉明
 */
public class LockInterceptor implements MethodInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    public LockInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //fix 使用其他aop组件时,aop切了两次.
        Class<?> cls = AopProxyUtils.ultimateTargetClass(invocation.getThis());
        if (!cls.equals(invocation.getThis().getClass())) {
            return invocation.proceed();
        }
        return executeLock(invocation);
    }
    
    /**
     * 1、@Lock4j 的属性配置会替换配置文件的配置 
     * @param invocation
     * @return
     */
    private Object executeLock(MethodInvocation invocation) throws Throwable {
        Lock4j lock4j = null;
        ConfigurableBeanFactory factory = (ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory();
        LockProperties lockProperties = factory.getBean(LockProperties.class);
        // 默认锁方式
        LockMode lockMode = lockProperties.getBinder();
        // 配置文件
        LockProducer lockProducer = factory.getBean(LockProducer.class);
        
        lock4j = invocation.getMethod().getAnnotation(Lock4j.class);
        // 锁的业务名称
        String businessName = lock4j.name();
        String key = lock4j.key();
        long acquireTimeout = lock4j.acquireTimeout();
        
        // 未配置key
        if ("".equals(key)) {
            // 读取配置
            LockConfigurationProperties lockConfigurationProperties = lockProducer.getLockConfigurationProperties(businessName);
            lockMode = lockConfigurationProperties.getBinder();
            key = lockConfigurationProperties.getKey();
        }
        // 未配置超时时间
        if (Long.MAX_VALUE == acquireTimeout) {
            // 读取配置
            LockConfigurationProperties lockConfigurationProperties = lockProducer.getLockConfigurationProperties(businessName);
            boolean block = lockConfigurationProperties.isBlock();
            acquireTimeout = lockConfigurationProperties.getAcquireTimeout();
            if (block) {
                acquireTimeout = -1;
            }
        }
        
        Lock lock = null;
        // 调用tryLock是否成功
        boolean tryLockStatus = false;
        try {
            LockFactory lockFactory = applicationContext.getBean(LockFactory.class);
            LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(lockMode);
    
            // 获取锁超时时间
            // 锁的key
            key = ValueUtil.resolveExpression(key, invocation.getMethod(), invocation.getArguments(),
                    invocation.getThis(),
                    (ConfigurableBeanFactory) applicationContext.getAutowireCapableBeanFactory(),
                    (ConfigurableEnvironment) applicationContext.getEnvironment());
    
            lock = lockTemplate.getLock(key);
    
            if (acquireTimeout < 0) {
                // lock
                logger.info("lock()->{},{}", lockMode, key);
                lock.lock();
                tryLockStatus = true;
                return invocation.proceed();
            } else {
                // tryLock
                if (acquireTimeout == 0) {
                    logger.info("tryLock()->{},{}", lockMode, key);
                    tryLockStatus = lock.tryLock();
                } else {
                    logger.info("tryLock({})->{},{}", acquireTimeout, lockMode, key);
                    tryLockStatus = lock.tryLock(acquireTimeout, TimeUnit.MILLISECONDS);
                }
                if (tryLockStatus) {
                    logger.info("Thread:{},tryLock({})->{},{}", Thread.currentThread().getName(), acquireTimeout, lockMode, key);
                    return invocation.proceed();
                }
            }
        } finally {
            if (lock != null && acquireTimeout < 0) {
                boolean flag = lock.unlock();
                logger.info("lock->unlock()->{},{},{}", lockMode, key, flag);
            } else if (lock != null && tryLockStatus) {
                boolean flag = lock.unlock();
                logger.info("tryLock->unlock()->{},{},{}", lockMode, key, flag);
            }
        }
        return null;
    }
    
}
