package com.fqm.test.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 文件状态通知aop处理器。弃用
 * 
 * @version 
 * @author 傅泉明
 */
public class FileUseNotifyInterceptor implements MethodInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;
    private ApplicationEventPublisher publisher;

    public FileUseNotifyInterceptor(ApplicationContext applicationContext, 
            ApplicationEventPublisher publisher) {
        this.applicationContext = applicationContext;
        this.publisher = publisher;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //fix 使用其他aop组件时,aop切了两次.
        Class<?> cls = AopProxyUtils.ultimateTargetClass(invocation.getThis());
        if (!cls.equals(invocation.getThis().getClass())) {
            return invocation.proceed();
        }
        FileUseNotify lock4j = null;
//        ConfigurableBeanFactory factory = (ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory();
//        LockProperties lockProperties = factory.getBean(LockProperties.class);
//
//        lock4j = invocation.getMethod().getAnnotation(FileUseNotify.class);
        // 锁的业务名称
//        String businessName = lock4j.name();
//        if (StringUtils.isNoneBlank(businessName)) {
//            businessName = ValueUtil.resolveExpression(factory, businessName).toString();
//            return propertiesLock(invocation, businessName);
//        } else {
//            return annotationLock(invocation, lock4j, factory, lockMode);
//        }
        try {
            System.out.println("--->start");
            // 还未开启事务，该事务不能生效，在事务前执行的。。。
//            ApplicationEventPublisher eventPublisher = applicationContext.getBean(ApplicationEventPublisher.class);
//            publisher.publishEvent(new FileEvent(new Date()));
            return invocation.proceed();
        } finally {
            System.out.println("--->end");
        }
    }
//    /**
//     * 注解获取锁
//     * @param invocation
//     * @param lock4j
//     * @param factory
//     * @param lockMode
//     * @return
//     * @throws Throwable
//     */
//    private Object annotationLock(MethodInvocation invocation, Lock4j lock4j, ConfigurableBeanFactory factory, LockMode lockMode) throws Throwable {
//        // 注解方式
//        // 调用tryLock是否成功
//        boolean tryLockStatus = false;
//        Lock lock = null;
//        // 锁的key
//        String key = null;
//        // 是否调用lock
//        boolean block = lock4j.block();
//        try {
//            LockFactory lockFactory = applicationContext.getBean(LockFactory.class);
//            LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(lockMode);
//
//            // 获取锁超时时间
//            long acquireTimeout = lock4j.acquireTimeout();
//            // 锁的key
//            key = lock4j.key();
//
//            key = ValueUtil.resolveExpression(factory, key).toString();
//
//            lock = lockTemplate.getLock(key);
//
//            if (block) {
//                // lock
//                logger.info("lock()->{},{}", lockMode, key);
//                lock.lock();
//                tryLockStatus = true;
//                return invocation.proceed();
//            } else {
//                // tryLock
//                if (acquireTimeout <= 0) {
//                    logger.info("tryLock()->{},{}", lockMode, key);
//                    tryLockStatus = lock.tryLock();
//                } else {
//                    logger.info("tryLock({})->{},{}", lock4j.acquireTimeout(), lockMode, key);
//                    tryLockStatus = lock.tryLock(lock4j.acquireTimeout(), TimeUnit.MILLISECONDS);
//                }
//                if (tryLockStatus) {
//                    return invocation.proceed();
//                }
//            }
//        } finally {
//            if (lock != null && block) {
//                boolean flag = lock.unlock();
//                logger.info("lock->unlock()->{},{},{}", lockMode, key, flag);
//            } else if (lock != null && tryLockStatus) {
//                boolean flag = lock.unlock();
//                logger.info("tryLock->unlock()->{},{},{}", lockMode, key, flag);
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 配置文件获取锁
//     * @param invocation
//     * @param key
//     * @param lockMode
//     * @param businessName
//     */
//    private Object propertiesLock(MethodInvocation invocation, String businessName) throws Throwable {
//        boolean lockFlag = false;
//        com.fqm.framework.locks.config.LockProducer.Lock lock = null;
//        String key = null;
//        LockMode lockMode = null;
//        try {
//            LockProducer lockProducer = applicationContext.getBean(LockProducer.class);
//            Producer producer = lockProducer.getProducer(businessName);
//            lock = producer.getLock();
//            key = producer.getKey();
//            lockMode = producer.getLockMode();
//            lockFlag = lock.lock();
//            if (lockFlag) {
//                logger.info("lock()->{},{}", lockMode, key);
//                return invocation.proceed();
//            }
//        } finally {
//            if (null != lock && lockFlag) {
//                boolean flag = lock.unLock();
//                logger.info("lock->unlock()->{},{},{}", lockMode, key, flag);
//            }
//        }
//        return null;
//    }

}
