package com.fqm.test.controller;

import java.util.concurrent.atomic.AtomicInteger;

import com.fqm.test.model.User;

import cn.hutool.core.date.LocalDateTimeUtil;

public class BaseController {

    AtomicInteger atomicInteger = new AtomicInteger();
    
    public User getUser() {
        User user = new User();
        user.setAge(atomicInteger.incrementAndGet());
        user.setName("张三");
        return user;
    }
    
    public User getDelayUser() {
        User user = getUser();
        user.setName("Delay:" + user.getName() + ":" + LocalDateTimeUtil.now().toString());
        return user;
    }
    
}
