package com.fqm.test.lock.controller;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.locks.Lock;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;
import com.fqm.test.lock.service.UserService;

import cn.hutool.core.thread.ThreadUtil;

@RestController
public class LockController {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    UserService userService;
    
    @Resource
    LockFactory lockFactory;
    
    @GetMapping("/lock/lock4j")
    public Object lockRedisTemplate() {
        System.out.println("lock4j");
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                userService.getUserByLock4jLock();
            }
        });
        return new HashMap<>();
    }
    
    @GetMapping("/lock/code")
    public Object lockCode() {
        System.out.println("lockCode");
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.zookeeper;
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
        
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.redis;
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
        
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.redisson;
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
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.zookeeper;
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
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.redis;
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
        ThreadUtil.concurrencyTest(5, new Runnable() {
            @Override
            public void run() {
                LockMode lockMode = LockMode.redisson;
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
    
}
