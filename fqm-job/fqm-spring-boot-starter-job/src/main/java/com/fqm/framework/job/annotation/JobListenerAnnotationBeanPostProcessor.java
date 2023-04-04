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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.fqm.framework.job.JobMode;
import com.fqm.framework.job.config.JobConfigurationProperties;
import com.fqm.framework.job.config.JobProperties;
import com.fqm.framework.job.listener.JobListenerParam;

/**
 * @JobListener 注解监听，并转换为 List<JobListenerParam> 对象
 * @version 
 * @author 傅泉明
 */
public class JobListenerAnnotationBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton, ApplicationContextAware {

    /** 获取使用 @MqListener 的类及方法  */
    private Map<JobMode, List<JobListenerParam>> listenerParams = new EnumMap<>(JobMode.class);
    
    private List<ListenerMethod> listenerMethods = new ArrayList<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        final List<ListenerMethod> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<JobListener> listenerAnnotations = findListenerAnnotations(method);
            if (!listenerAnnotations.isEmpty()) {
                methods.add(new ListenerMethod(bean, method, listenerAnnotations.toArray(new JobListener[listenerAnnotations.size()])));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        if (!methods.isEmpty()) {
            listenerMethods.addAll(methods);
        }

        return bean;
    }

    public List<JobListenerParam> getListeners(JobMode jobMode) {
        return listenerParams.get(jobMode);
    }
    
    private Collection<JobListener> findListenerAnnotations(AnnotatedElement element) {
        // .map(ann -> ann.synthesize()).collect(Collectors.toList())
        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY)
                .stream(JobListener.class)
                .map(MergedAnnotation::synthesize)
                .collect(Collectors.toCollection(ArrayList<JobListener>::new))
                ;
    }

    private static class ListenerMethod {
        final Object bean;
        final Method method; // NOSONAR
        final JobListener[] listeners; // NOSONAR

        ListenerMethod(Object bean, Method method, JobListener[] listeners) { // NOSONAR
            this.bean = bean;
            this.method = method;
            this.listeners = listeners;
        }

    }
    
    private void addListenerParam(JobMode jobMode, JobListenerParam listenerParam) {
        List<JobListenerParam> listeners = this.getListeners(jobMode);
        if (null == listeners) {
            listeners = new ArrayList<>();
            listenerParams.put(jobMode, listeners);
        }
        listeners.add(listenerParam);
    }

    @Override
    public void afterSingletonsInstantiated() {
        JobProperties jobProperties = applicationContext.getBean(JobProperties.class);
        // @JobListener 校验配置文件和属性
        for (ListenerMethod method : listenerMethods) {
            for (JobListener listener : method.listeners) {
                String name = listener.name();
                Assert.isTrue(StringUtils.hasText(name), "Please specific [name] under @JobListener.");
                
                JobConfigurationProperties properties = jobProperties.getJobs().get(name);
                Assert.isTrue(null != properties, "@JobListener attribute name is [" + name + "], not found in the configuration [job.jobs." + name + "],[" + method.bean.getClass().getName() + "],[" + method.method.getName() + "]");
                
                JobMode jobMode = properties.getBinder();
                if (null == jobMode) {
                    jobMode = jobProperties.getBinder();
                }
                Assert.isTrue(null != jobMode, "Please specific [binder] under [job.jobs." + name + "] configuration  or [binder] under [job] configuration.");
                
                JobListenerParam param = new JobListenerParam();
                param.setName(name).setBean(method.bean).setMethod(method.method);
                addListenerParam(jobMode, param);
            }
        }
        listenerMethods.clear();
    }
}