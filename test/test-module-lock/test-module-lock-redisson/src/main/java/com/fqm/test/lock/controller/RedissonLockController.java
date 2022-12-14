package com.fqm.test.lock.controller;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.locks.Lock;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;
import com.fqm.framework.locks.annotation.Lock4j;
import com.fqm.framework.locks.template.RedissonLockTemplate;

import cn.hutool.core.thread.ThreadUtil;

@RestController
public class RedissonLockController {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    RedissonLockUserService lockUserService;
    
    @Resource
    LockFactory lockFactory;
    
    @GetMapping("/lock4j/redisson")
    public Object lockSimpleTemplate() {
        System.out.println("lockRedissonTemplate");
        ThreadUtil.concurrencyTest(3, new Runnable() {
            @Override
            public void run() {
                lockUserService.getUserByLock4jLock();
            }
        });
        return new HashMap<>();
    }
    
    @GetMapping("/lock/redisson")
    public Object lockCode() {
        System.out.println("lock/redisson");
        ThreadUtil.concurrencyTest(3, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.REDISSON;
                logger.info("lockCode->{},{}", Thread.currentThread().getId(), lockMode);
                Lock lock = lockFactory.getLockTemplate(lockMode).getLock("1");
                lock.lock();
                try {
                    logger.info("lockCode->{},{}", lockMode, Thread.currentThread().getId());
                    TimeUnit.SECONDS.sleep(2);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
        System.out.println("--------------------------------------------------------------");
        //--------------------------------------------------------------
        ThreadUtil.concurrencyTest(3, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.REDISSON;
                logger.info("lockCode->{},{}", Thread.currentThread().getId(), lockMode);
                Lock lock = lockFactory.getLockTemplate(lockMode).getLock("1");
                boolean flag = lock.tryLock();
                try {
                    if (flag) {
                        logger.info("lockCode->{},{}", lockMode, Thread.currentThread().getId());
                        TimeUnit.SECONDS.sleep(2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (flag) {
                        lock.unlock();
                    }
                }
            }
        });
        return new HashMap<>();
    }
    
    @Service
    class RedissonLockUserService {
        @Lock4j(key = "${lock.redisson.key}", block = true, lockTemplate = RedissonLockTemplate.class, lockMode = "${lock.redisson.lockMode}")
        public Object getUserByLock4jLock() {
            logger.info("RedissonLockUserService");
            HashMap<String, Object> user = new HashMap<>();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return user;
        }
    }
}


