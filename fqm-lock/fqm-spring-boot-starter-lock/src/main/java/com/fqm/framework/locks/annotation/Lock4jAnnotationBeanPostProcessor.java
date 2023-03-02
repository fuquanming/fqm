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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.StartupStep;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.fqm.framework.common.spring.util.SpringUtil;
import com.fqm.framework.locks.LockFactory;
import com.fqm.framework.locks.LockMode;
import com.fqm.framework.locks.config.LockConfigurationProperties;
import com.fqm.framework.locks.config.LockProperties;
import com.fqm.framework.locks.template.LockTemplate;

/**
 * @Lock4j 注解监听、判断配置是否加载锁组件
 * @version 
 * @author 傅泉明
 */
public class Lock4jAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

    private LockProperties lockProperties;

    public Lock4jAnnotationBeanPostProcessor(LockProperties lockProperties) {
        this.lockProperties = lockProperties;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        final List<ListenerMethod> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<Lock4j> listenerAnnotations = findListenerAnnotations(method);
            if (!listenerAnnotations.isEmpty()) {
                methods.add(new ListenerMethod(method, listenerAnnotations.toArray(new Lock4j[listenerAnnotations.size()])));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        // @Lock4j 校验配置文件和属性
        if (!methods.isEmpty()) {
            for (ListenerMethod method : methods) {
                for (Lock4j lock4j : method.lock4j) {
                    // 锁的业务名称
                    String name = lock4j.name();
                    LockConfigurationProperties properties = lockProperties.getLocks().get(name);
                    Assert.isTrue(null != properties,"@Lock4j attribute name is [" + name + "], not found in the configuration [lock.locks." + name + "],["+ bean.getClass().getName() + "],[" + method.method.getName() + "]");
                    // 锁的名称
                    String key = properties.getKey();
                    Assert.hasText(key, "Please specific [key] under [lock.locks." + name + "] configuration.");
                    // 锁的方式
                    LockMode lockMode = properties.getBinder();
                    if (null == lockMode) {
                        lockMode = lockProperties.getBinder();
                    }
                    Assert.isTrue(null != lockMode, "Please specific [binder] under [lock.locks." + name + "] configuration or [binder] under [lock] configuration.");
                }
            }
        }
        // 判断配置是否加载锁组件

        return bean;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Collection<Lock4j> findListenerAnnotations(AnnotatedElement element) {
        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).stream(Lock4j.class).map(MergedAnnotation::synthesize)
                .collect(Collectors.toList());
    }

    private static class ListenerMethod {

        final Method method; // NOSONAR

        final Lock4j[] lock4j; // NOSONAR

        ListenerMethod(Method method, Lock4j[] lock4j) { // NOSONAR
            this.method = method;
            this.lock4j = lock4j;
        }

    }

}