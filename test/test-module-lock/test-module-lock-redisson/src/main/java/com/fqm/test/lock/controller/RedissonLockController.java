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
public class RedissonLockController {
    
    private Logger logger = LoggerFactory.getLogger(RedissonLockController.class);

    @Resource
    RedissonLockUserService lockUserService;
    @Resource
    LockProducer lockProducer;
    
    public static final String BUSINESS_NAME = "redisson";
    public static final String BUSINESS_NAME_1 = "redisson1";
    
    public String testName(Long a) {
        return "张三1:" + a;
    }
    
    @GetMapping("/lock4j/redisson")
    public Object lock4j() {
        System.out.println("lock4j-1");
        CyclicBarrier cb = new CyclicBarrier(2);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                ThreadUtil.concurrencyTest(1, new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Thread:{},begin", Thread.currentThread().getName());
                        Object obj = null;
                        try {
                            TestUser testUser = new TestUser();
                            testUser.setId(111L);
                            obj = lockUserService.getUserByLock4jLock(testUser);
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
                ThreadUtil.concurrencyTest(1, new Runnable() {
                    @Override
                    public void run() {
                        logger.info("Thread2:{},begin", Thread.currentThread().getName());
                        Object obj = null;
                        try {
                            TestUser testUser = new TestUser();
                            testUser.setId(111L);
                            obj = lockUserService.getUserByLock4jLock(testUser);
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
    
    @GetMapping("/lock/redisson")
    public Object lockCode() {
        System.out.println("lock/redisson");
        CyclicBarrier cb = new CyclicBarrier(1);
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
class RedissonLockUserService {
    
    public String testName() {
        return "userService";
    }
    private Logger logger = LoggerFactory.getLogger(getClass());
//    @Lock4j(name = RedissonLockController.BUSINESS_NAME, key = "#testUser.id")
    // SpEL 中，${} 通常用于读取外部属性占位符​。引用方法参数时，应使用 #参数名 的语法。因此，正确的表达式应为 #testUser.id（或带前缀的拼接）。
    // 字符串用 '' 包含
//    @Lock4j(key = "'user:' + #testUser.id + '---' + ${server.port}")
    // 
    @Lock4j(
            name = "redisson"
            ,key = "'userQQ:' + #testUser.id + '-' + @redissonLockController.testName(#testUser.id) + '-'  + #this.testName + '-' + ${server.port}"
            , acquireTimeout = 6000L
            )
    public Object getUserByLock4jLock(TestUser testUser) {
        logger.info("Thread:{},RedissonLockUserService", Thread.currentThread().getName());
        HashMap<String, Object> user = new HashMap<>();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }
//    @Lock4j(name = RedissonLockController.BUSINESS_NAME_1)
    @Lock4j(key = "'user1:' + #testUser.id")
    public Object getUserByLock4jLock2(TestUser testUser) {
        logger.info("Thread2:{},RedissonLockUserService2", Thread.currentThread().getName());
        HashMap<String, Object> user = new HashMap<>();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }
}
