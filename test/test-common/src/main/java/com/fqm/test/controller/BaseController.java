package com.fqm.test.controller;

import java.util.concurrent.atomic.AtomicInteger;

import com.fqm.test.model.User;

public class BaseController {

    AtomicInteger atomicInteger = new AtomicInteger();
    
    public User getUser() {
        User user = new User();
        user.setAge(atomicInteger.incrementAndGet());
        user.setName("张三");
        return user;
    }
    
}
