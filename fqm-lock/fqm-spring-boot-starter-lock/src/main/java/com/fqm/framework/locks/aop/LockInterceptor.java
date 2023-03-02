package com.fqm.framework.locks.aop;

import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
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
import com.fqm.framework.locks.config.LockProducer;
import com.fqm.framework.locks.config.LockProperties;
import com.fqm.framework.locks.config.LockProducer.Producer;
import com.fqm.framework.locks.template.LockTemplate;

/**
 * 分布式锁aop处理器
 * 
 * @version 
 * @author 傅泉明
 */
public class LockInterceptor implements MethodInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private LockFactory lockFactory;

    private LockProducer lockProducer;

    private ApplicationContext applicationContext;

    public LockInterceptor(LockFactory lockFactory, LockProducer lockProducer, ApplicationContext applicationContext) {
        this.lockFactory = lockFactory;
        this.lockProducer = lockProducer;
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
        ConfigurableBeanFactory factory = (ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory();
        LockProperties lockProperties = factory.getBean(LockProperties.class);
        // 默认锁方式
        LockMode lockMode = lockProperties.getBinder();

        lock4j = invocation.getMethod().getAnnotation(Lock4j.class);
        // 锁的业务名称
        String businessName = lock4j.name();
        if (StringUtils.isNoneBlank(businessName)) {
            businessName = ValueUtil.resolveExpression(factory, businessName).toString();
            return propertiesLock(invocation, businessName);
        } else {
            return annotationLock(invocation, lock4j, factory, lockMode);
        }
    }
    /**
     * 注解获取锁
     * @param invocation
     * @param lock4j
     * @param factory
     * @param lockMode
     * @return
     * @throws Throwable
     */
    private Object annotationLock(MethodInvocation invocation, Lock4j lock4j, ConfigurableBeanFactory factory, LockMode lockMode) throws Throwable {
        // 注解方式
        // 调用tryLock是否成功
        boolean tryLockStatus = false;
        Lock lock = null;
        // 锁的key
        String key = null;
        // 是否调用lock
        boolean block = lock4j.block();
        try {

            LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(lockMode);

            // 获取锁超时时间
            long acquireTimeout = lock4j.acquireTimeout();
            // 锁的key
            key = lock4j.key();

            key = ValueUtil.resolveExpression(factory, key).toString();

            Assert.hasText(key, "lock key must be not empty, see the @Lock4j");

            lock = lockTemplate.getLock(key);

            if (block) {
                // lock
                logger.info("lock()->{},{}", lockMode, key);
                lock.lock();
                tryLockStatus = true;
                return invocation.proceed();
            } else {
                // tryLock
                if (acquireTimeout <= 0) {
                    logger.info("tryLock()->{},{}", lockMode, key);
                    tryLockStatus = lock.tryLock();
                } else {
                    logger.info("tryLock({})->{},{}", lock4j.acquireTimeout(), lockMode, key);
                    tryLockStatus = lock.tryLock(lock4j.acquireTimeout(), TimeUnit.MILLISECONDS);
                }
                if (tryLockStatus) {
                    return invocation.proceed();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("lock4j error", e);
        } finally {
            if (lock != null && block) {
                boolean flag = lock.unlock();
                logger.info("lock->unlock()->{},{},{}", lockMode, key, flag);
            } else if (lock != null && tryLockStatus) {
                boolean flag = lock.unlock();
                logger.info("tryLock->unlock()->{},{},{}", lockMode, key, flag);
            }
        }
        return null;
    }

    /**
     * 配置文件获取锁
     * @param invocation
     * @param key
     * @param lockMode
     * @param businessName
     */
    private Object propertiesLock(MethodInvocation invocation, String businessName) {
        boolean lockFlag = false;
        com.fqm.framework.locks.config.LockProducer.Lock lock = null;
        String key = null;
        LockMode lockMode = null;
        try {
            Producer producer = lockProducer.getProducer(businessName);
            lock = producer.getLock();
            key = producer.getKey();
            lockMode = producer.getLockMode();
            lockFlag = lock.lock();
            if (lockFlag) {
                logger.info("lock()->{},{}", lockMode, key);
                return invocation.proceed();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("lock4j error", e);
        } finally {
            if (null != lock && lockFlag) {
                boolean flag = lock.unLock();
                logger.info("lock->unlock()->{},{},{}", lockMode, key, flag);
            }
        }
        return null;
    }

}
