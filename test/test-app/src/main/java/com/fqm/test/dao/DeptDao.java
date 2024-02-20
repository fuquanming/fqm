package com.fqm.test.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fqm.test.model.Dept;

@Repository
public interface DeptDao extends BaseMapper<Dept> {

    Dept getById(@Param("id") Long id);

}