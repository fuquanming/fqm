package com.fqm.test.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fqm.test.aop.FileUseNotify;
import com.fqm.test.dao.DeptDao;
import com.fqm.test.event.FileEvent;
import com.fqm.test.model.Dept;

/**
 * extends ServiceImpl<DeptDao, Dept> 导致 第二次动态加载，找不到Dao。
 * 
 * @version 
 * @author 傅泉明
 */
@Service
public class DeptService extends ServiceImpl<DeptDao, Dept> {

    @Resource
    private DeptDao deptDao;
    
    @Resource
    ApplicationEventPublisher publisher;
    
    @Resource
    TestService testService;

    @Transactional(rollbackFor = Exception.class)
//    @FileUseNotify
    public Dept insert(Dept data) {
//        int i = 1 / 0;// 回调 FileListener.AFTER_ROLLBACK
        data.setName("2");
        
//        deptDao.insert(data);
//        data.setName("1");
//        data.setId(1L);
        testService.testNotify(data);
        deptDao.insert(data);
//        int i1 = 1 / 0;// 回调 FileListener.AFTER_ROLLBACK
        // 这边保存数据，拦截请求
        
        return data;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public Dept update2() {
        Dept dept = deptDao.getById(1L);
        dept.setName("1");
        testService.testNotify(dept);
        deptDao.updateById(dept);
        return dept;
    }

    public Dept getById(Long id) {
        return deptDao.getById(id);
    }
    
    @FileUseNotify
    public Dept getById2(Dept data) {
        System.out.println("---getById2---");
//        insert(data);
        return new Dept();
    }

}
