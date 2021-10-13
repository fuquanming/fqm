package com.fqm.framework.common.lock;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.fqm.framework.common.lock.impl.SimpleLock;
import com.fqm.framework.common.lock.template.SimpleLockTemplate;

public class TestLock {

    @Test
    public void simleLockTest() {
        
        LockFactory lockFactory = new LockFactory();
        
        lockFactory.addLockTemplate(new SimpleLockTemplate());
        
        Lock lock = lockFactory.getLockTemplate(SimpleLockTemplate.class)
                .getLock("123");
        lock.lock();
        try {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(Thread.currentThread().getId() + "->getLock");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
    
}
