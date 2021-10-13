/*
 *  Copyright (c) 2018-2021, baomidou (63976799@qq.com).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.fqm.framework.common.lock.aop;

import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.fqm.framework.common.lock.Lock;
import com.fqm.framework.common.lock.LockFactory;
import com.fqm.framework.common.lock.annotation.Lock4j;
import com.fqm.framework.common.lock.template.LockTemplate;
import com.fqm.framework.common.spring.util.ValueUtil;

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
        boolean tryLockFlag = false;// 是否调用tryLock
        boolean tryLockStatus = false;// 调用tryLock是否成功
        
        Lock lock = null;
        boolean block = false;// 是否调用lock
        
        String lockName = null;
        String key = null;// 锁的key
        
        try {
            lock4j = invocation.getMethod().getAnnotation(Lock4j.class);
            
            block = lock4j.block();// 是否调用lock
            
            LockTemplate<?> lockTemplate = lockFactory.getLockTemplate(lock4j.lockTemplate());
            
            long acquireTimeout = lock4j.acquireTimeout();// 获取锁超时时间
            
            key = lock4j.key();// 锁的key
            
            key = ValueUtil.resolveExpression((ConfigurableBeanFactory) this.applicationContext.getAutowireCapableBeanFactory(), key).toString();
            
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
        } finally {
            if (lock != null && block) {
                lock.unlock();
                logger.info("lock->unlock()->{},{}", lockName, key);
            }
            if (lock != null && tryLockFlag && tryLockStatus) {
                lock.unlock();
                logger.info("tryLock->unlock()->{},{}", lockName, key);
            }
        }
    }

}
