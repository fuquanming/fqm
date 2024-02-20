package com.fqm.test.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;

/**
 * 分布式锁aop通知，
 * Advisor 会在其他类之前加载，可能不能被所有的 BeanPostProcessor 过滤，启动时会出现提示信息，通过实现BeanPostProcessor来规避
 * 
 * @version 
 * @author 傅泉明
 */
public class FileUseNotifyAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanPostProcessor {

    private static final long serialVersionUID = 1L;

    private final transient Advice advice;

    private final transient Pointcut pointcut = AnnotationMatchingPointcut.forMethodAnnotation(FileUseNotify.class);

    public FileUseNotifyAnnotationAdvisor(@NonNull FileUseNotifyInterceptor lockInterceptor) {
        this.advice = lockInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }
    
    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }
    
    @Override
    public int hashCode() {
        return PointcutAdvisor.class.hashCode();
    }
    
}