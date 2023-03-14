package com.fqm.test.lock.controller;

import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fqm.framework.locks.annotation.Lock4j;
import com.fqm.framework.locks.config.LockProducer;
import com.fqm.framework.locks.config.LockProducer.Lock;

import cn.hutool.core.thread.ThreadUtil;

@RestController
public class RedisLockController {
    
    private Logger logger = LoggerFactory.getLogger(RedisLockController.class);

    @Resource
    RedisLockUserService lockUserService;
    @Resource
    LockProducer lockProducer;
    
    public static final String BUSINESS_NAME = "redis";
    public static final String BUSINESS_NAME_1 = "redis1";
    
    @GetMapping("/lock4j/redis")
    public Object lock4j() {
        System.out.println("lock4j");
        CyclicBarrier cb = new CyclicBarrier(2);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                ThreadUtil.concurrencyTest(3, new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Thread:{},begin", Thread.currentThread().getName());
                        Object obj = null;
                        try {
                            obj = lockUserService.getUserByLock4jLock();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        logger.info("Thread:{},end,{}", Thread.currentThread().getName(), obj);
                    }
                });
            }
        });
        t1.start();
        
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                ThreadUtil.concurrencyTest(3, new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Thread2:{},begin", Thread.currentThread().getName());
                        Object obj = null;
                        try {
                            obj = lockUserService.getUserByLock4jLock2();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        logger.info("Thread2:{},end,{}", Thread.currentThread().getName(), obj);
                    }
                });
            }
        });
        t2.start();
        
        return new HashMap<>();
    }
    
    @GetMapping("/lock/redis")
    public Object lockCode() {
        System.out.println("lock/redis");
        CyclicBarrier cb = new CyclicBarrier(2);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                ThreadUtil.concurrencyTest(3, new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Thread:{},begin", Thread.currentThread().getName());
                        Lock lock = null;
                        boolean flag = false;
                        try {
                            lock = lockProducer.getLock(BUSINESS_NAME);
                            flag = lock.lock();
                            if (flag) {
                                logger.info("Thread:{},lock", Thread.currentThread().getName());
                                TimeUnit.SECONDS.sleep(2);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (flag) {
                                flag = lock.unLock();
                                logger.info("Thread:{},unlock", Thread.currentThread().getName());
                            }
                        }
                    }
                });
            }
        });
        t1.start();
        
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                ThreadUtil.concurrencyTest(3, new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Thread2:{},begin", Thread.currentThread().getName());
                        Lock lock = null;
                        boolean flag = false;
                        try {
                            lock = lockProducer.getLock(BUSINESS_NAME_1);
                            flag = lock.lock();
                            if (flag) {
                                logger.info("Thread2:{},lock", Thread.currentThread().getName());
                                TimeUnit.SECONDS.sleep(2);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (flag) {
                                flag = lock.unLock();
                                logger.info("Thread2:{},unlock", Thread.currentThread().getName());
                            }
                        }
                    }
                });
            }
        });
        t2.start();
        
        
        return new HashMap<>();
    }
    
}

@Service
class RedisLockUserService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Lock4j(name = RedisLockController.BUSINESS_NAME)
    public Object getUserByLock4jLock() {
        logger.info("Thread:{},RedisLockUserService", Thread.currentThread().getName());
        HashMap<String, Object> user = new HashMap<>();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }
    @Lock4j(name = RedisLockController.BUSINESS_NAME_1)
    public Object getUserByLock4jLock2() {
        logger.info("Thread2:{},RedisLockUserService2", Thread.currentThread().getName());
        HashMap<String, Object> user = new HashMap<>();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }
}


