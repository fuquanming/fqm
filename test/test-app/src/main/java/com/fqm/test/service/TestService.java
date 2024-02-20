package com.fqm.test.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fqm.test.aop.FileUseNotify;
import com.fqm.test.dao.DeptDao;
import com.fqm.test.model.Dept;

@Service
public class TestService extends ServiceImpl<DeptDao, Dept> {

    @FileUseNotify
    public List<String> testNotify(Dept data) {
        System.out.println("---testNotify---");
        Dept d = this.getById(1L);
        System.out.println("->findName=" + d.getName() + ",tranName=" + 
                TransactionSynchronizationManager.getCurrentTransactionName());
        return Collections.singletonList("a/a.png");
    }
    
    @FileUseNotify(isTransaction = false)
    public List<String> testNotify() {
        System.out.println("---testNotify---2---");
        return Collections.singletonList("a/b.png");
    }
    
    @FileUseNotify(isTransaction = false)
    public void testNotifyError() {
        System.out.println("---testNotify---3---");
    }
    
}
