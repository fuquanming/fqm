package com.fqm.test.controller;

import com.fqm.framework.common.core.util.IdUtil;
import com.fqm.framework.common.core.util.JsonUtil;
import com.fqm.framework.common.core.vo.Result;
import com.fqm.framework.common.spring.util.SpringUtil;
import com.fqm.test.config.BeanReferenceTracker;
import com.fqm.test.model.Dept;
import com.fqm.test.service.DeptService;
import com.fqm.test.service.TestService;
import io.swagger.annotations.ApiOperation;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

@RestController
public class DeptController {

    @Resource
    DeptService deptService;
    @Resource
    TestService testService;
    @Resource
    BeanReferenceTracker beanReferenceTracker;
    
    @ApiOperation("新增用户")
    @GetMapping("/dept/insert")
    public Result<Dept> insertDept() {
        Dept data = new Dept();
        data.setId(IdUtil.getSnowflake().nextId());
        data.setName("1");// 数据库有唯一索引
        data.setCreateTime(new Date());
//        deptService.insert(data);
//        deptService.update2();
        
//        testService.testNotify(data);
//        testService.testNotify();
//        testService.testNotifyError();
        
        

//        deptService.getById2(data);
        
        System.out.println("---insertDept---");
        
        DeptController bean = SpringUtil.getBean(DeptController.class);
        Class<?> targetClass = bean.getClass();
        ReflectionUtils.doWithFields(targetClass, field -> {
            if (targetClass == DeptController.class) {
                System.out.println("-------findClass---" + field.getType());
                if (field.getType() == TestService.class) {
                    System.out.println("-------find---" + field.getName());
                }
            }
        }, ReflectionUtils.COPYABLE_FIELDS);
        
        return Result.ok(data);
    } 
    
}
