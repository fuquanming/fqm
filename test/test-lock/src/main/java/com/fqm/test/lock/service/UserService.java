package com.fqm.test.lock.service;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fqm.framework.locks.annotation.Lock4j;
import com.fqm.framework.locks.template.RedissonLockTemplate;

@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Lock4j(key = "${lock.user.key}", block = true, lockTemplate = RedissonLockTemplate.class, lockMode = "${lock.user.lockMode}")
    public Object getUserByLock4jLock() {
        logger.info("getUserByLock4jLock");
        HashMap<String, Object> user = new HashMap<>();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }
}
