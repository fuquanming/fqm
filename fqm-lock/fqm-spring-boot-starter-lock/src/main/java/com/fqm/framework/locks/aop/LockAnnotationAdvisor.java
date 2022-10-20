package com.fqm.framework.locks.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.NonNull;

import com.fqm.framework.locks.annotation.Lock4j;

/**
 * 分布式锁aop通知
 * 
 * @version 
 * @author 傅泉明
 */
public class LockAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    private static final long serialVersionUID = 1L;

    private final transient Advice advice;

    private final transient Pointcut pointcut = AnnotationMatchingPointcut.forMethodAnnotation(Lock4j.class);

    public LockAnnotationAdvisor(@NonNull LockInterceptor lockInterceptor, int order) {
        this.advice = lockInterceptor;
        setOrder(order);
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
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
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