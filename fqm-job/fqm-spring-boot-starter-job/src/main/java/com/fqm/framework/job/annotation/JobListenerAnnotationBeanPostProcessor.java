/*
 * @(#)JobListenerAnnotationBeanPostProcessor.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-job
 * 创建日期 : 2022年9月2日
 * 修改历史 : 
 *     1. [2022年9月2日]创建文件 by 傅泉明
 */
package com.fqm.framework.job.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.fqm.framework.common.spring.util.ValueUtil;
import com.fqm.framework.job.listener.JobListenerParam;

/**
 * @JobListener 注解监听，并转换为 List<JobListenerParam> 对象
 * @version 
 * @author 傅泉明
 */
public class JobListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered, BeanFactoryAware {

    private BeanFactory beanFactory;

    private List<JobListenerParam> listeners = new ArrayList<>();

    private Map<String, JobListenerParam> jobListeners = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        final List<ListenerMethod> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<JobListener> listenerAnnotations = findListenerAnnotations(method);
            if (!listenerAnnotations.isEmpty()) {
                methods.add(new ListenerMethod(method, listenerAnnotations.toArray(new JobListener[listenerAnnotations.size()])));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        if (!methods.isEmpty()) {
            for (ListenerMethod method : methods) {
                for (JobListener listener : method.annotations) {
                    // jobName
                    String name = listener.name();
                    String nameStr = ValueUtil.resolveExpression((ConfigurableBeanFactory) beanFactory, name).toString();
                    Assert.isTrue(StringUtils.hasText(nameStr), "Please specific [name] under job configuration.");
                    JobListenerParam param = new JobListenerParam();
                    param.setName(nameStr).setBean(bean).setMethod(method.method);
                    listeners.add(param);
                }
            }
        }

        return bean;
    }

    public List<JobListenerParam> getListeners() {
        return listeners;
    }

    public Map<String, JobListenerParam> getJobListeners() {
        return jobListeners;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Collection<JobListener> findListenerAnnotations(AnnotatedElement element) {
        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).stream(JobListener.class).map(ann -> ann.synthesize())
                .collect(Collectors.toList());
    }

    private static class ListenerMethod {

        final Method method; // NOSONAR

        final JobListener[] annotations; // NOSONAR

        ListenerMethod(Method method, JobListener[] annotations) { // NOSONAR
            this.method = method;
            this.annotations = annotations;
        }

    }
}