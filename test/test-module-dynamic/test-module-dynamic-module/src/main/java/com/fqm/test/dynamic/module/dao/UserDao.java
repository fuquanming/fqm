package com.fqm.test.dynamic.module.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fqm.test.model.User;

@Repository
public interface UserDao extends BaseMapper<User> {

    User getById(@Param("id") Long id);

}