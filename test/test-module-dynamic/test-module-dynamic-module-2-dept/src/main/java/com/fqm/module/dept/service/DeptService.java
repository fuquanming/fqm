package com.fqm.module.dept.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fqm.module.dept.dao.DeptDao;
import com.fqm.module.dept.model.Dept;

@Service
public class DeptService extends ServiceImpl<DeptDao, Dept> {

    @Resource
    private DeptDao deptDao;
    
    @Transactional(rollbackFor = Exception.class)
    public Dept insert(Dept data) {
        System.out.println("---DeptService222---");
        data.setName("张三");
        deptDao.insert(data);
        return data;
    }
    
    public Dept getById(Long id) {
        return deptDao.getById(id);
    }
    
}
