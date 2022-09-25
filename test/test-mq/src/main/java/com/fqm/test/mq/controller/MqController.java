package com.fqm.test.mq.controller;

import java.util.concurrent.atomic.AtomicInteger;

import com.fqm.test.mq.model.User;

public class MqController {

    AtomicInteger atomicInteger = new AtomicInteger();
    
    public User getUser() {
        User user = new User();
        user.setAge(atomicInteger.incrementAndGet());
        user.setName("张三");
        return user;
    }
    
}
