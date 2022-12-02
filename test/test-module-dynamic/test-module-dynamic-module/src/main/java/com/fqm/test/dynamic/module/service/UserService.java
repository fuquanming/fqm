package com.fqm.test.dynamic.module.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fqm.test.dynamic.module.dao.UserDao;
import com.fqm.test.model.User;

@Service
public class UserService extends ServiceImpl<UserDao, User> {

    @Resource
    private UserDao userDao;
    
    @Transactional(rollbackFor = Exception.class)
    public User insert(User user) {
        userDao.insert(user);
        return user;
    }
    
    public User getById(Long id) {
        return userDao.getById(id);
    }
    
}
