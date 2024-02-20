package com.fqm.test.aop;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fqm.framework.common.core.exception.ServiceException;
import com.fqm.test.event.FileEvent;

/**
 * 执行时已开启了事务，可以用事务发布事件。启用
 * 1、xxxService.a():a方法上有@Transactional
 * 2、xxxService.a()中调用xxxSer.b():b方法上有@FileUseNotify
 *   b方法:返回值实现接口... 
 * 
 * 
 * @version 
 * @author 傅泉明
 */
@Aspect
@Component
public class CustomTagAspect {
 
    @Resource
    private ApplicationEventPublisher publisher;
    
    @Around("@annotation(FileUseNotify)")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行前的逻辑
        // joinPoint.getTarget() 是拦截方法的所在类
        System.out.println("Before method execution");
        
 
        // 运行时，判断是否有开启事务
        String tranName = TransactionSynchronizationManager.getCurrentTransactionName();
        System.err.println("tranName=" + tranName);
        
        // 获取执行的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 注解校验
        FileUseNotify fileUseNotify = signature.getMethod().getAnnotation(FileUseNotify.class);
        boolean transaction = fileUseNotify.isTransaction();
        if (transaction && null == tranName) {
            throw new ServiceException(12, "未开启事务");
        }
        // 执行的方法返回值类型校验
        if (method.getReturnType() != List.class) {
            throw new ServiceException(12, "方法返回值类型不正确");
        }
        
        // 执行方法
        Object result = joinPoint.proceed();
        if (null != result) {
            publisher.publishEvent(new FileEvent(result));
        } else {
            throw new ServiceException(12, "方法返回值为null");
        }
        
//        Object[] args = joinPoint.getArgs();
//        if (args != null) {
//            for (Object obj : args) {
//                System.out.println(obj);
//            }
//        }
 
        // 执行后的逻辑
//        System.out.println("After method execution");
 
        return result;
    }
}
