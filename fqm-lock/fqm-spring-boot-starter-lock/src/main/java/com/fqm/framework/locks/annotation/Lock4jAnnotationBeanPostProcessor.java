/*
 * @(#)Lock4jAnnotationBeanPostProcessor.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-lock
 * 创建日期 : 2023年3月2日
 * 修改历史 : 
 *     1. [2023年3月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.locks.annotation;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.ReflectionUtils;

/**
 * @Lock4j 注解校验对应的配置文件
 * @version 
 * @author 傅泉明
 */
public class Lock4jAnnotationBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton {

    private List<ListenerMethod> lock4jMethods = new ArrayList<>();
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        final List<ListenerMethod> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<Lock4j> listenerAnnotations = findListenerAnnotations(method);
            if (!listenerAnnotations.isEmpty()) {
                methods.add(new ListenerMethod(bean.getClass().getName(), method.getName(), listenerAnnotations.toArray(new Lock4j[listenerAnnotations.size()])));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        if (!methods.isEmpty()) {
            lock4jMethods.addAll(methods);
        }
        return bean;
    }

    private Collection<Lock4j> findListenerAnnotations(AnnotatedElement element) {
        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).stream(Lock4j.class).map(MergedAnnotation::synthesize)
                .collect(Collectors.toList());
    }

    private static class ListenerMethod {

        final String methodName;
        
        final String className;

        final Lock4j[] lock4j; // NOSONAR

        ListenerMethod(String className, String methodName, Lock4j[] lock4j) { // NOSONAR
            this.className = className;
            this.methodName = methodName;
            this.lock4j = lock4j;
        }

    }

    @Override
    public void afterSingletonsInstantiated() {
//        LockProperties lockProperties = SpringUtil.getBean(LockProperties.class);
//        // 1、@Lock4j 校验配置文件和属性
//        for (ListenerMethod method : lock4jMethods) {
//            for (Lock4j lock4j : method.lock4j) {
//                // 锁的业务名称
//                String name = lock4j.name();
//                String key = lock4j.key();
//                if (StringUtils.hasText(name)) {
//                    Assert.isTrue(lockProperties.getLocks().containsKey(name), "@Lock4j attribute name is [" + name + "], not found in the configuration [lock.locks." + name + "],["+ method.className + "->" + method.methodName + "]");
//                } else if (StringUtils.hasText(key)) {
//                    // do nothing
//                } else {
//                    Assert.isTrue(false, "@Lock4j attribute name or key is required,["+ method.className + "->" + method.methodName + "]");
//                }
//            }
//        }
        lock4jMethods.clear();
    }

}