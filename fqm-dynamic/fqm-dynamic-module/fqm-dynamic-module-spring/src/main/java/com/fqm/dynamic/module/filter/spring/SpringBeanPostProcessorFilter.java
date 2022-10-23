/*
 * @(#)BaseSpringFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring
 * 创建日期 : 2022年6月27日
 * 修改历史 : 
 *     1. [2022年6月27日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.spring;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.fqm.framework.common.spring.util.SpringUtil;

/**
 * Spring 加载基类
 * @version 
 * @author 傅泉明
 */
public class SpringBeanPostProcessorFilter {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    /** 记录Spring代理使用类加载器 */
    private static ClassLoader springProxyClassLoader;
    
    public static ClassLoader getSpringProxyClassLoader() {
        return springProxyClassLoader;
    }
    
    public static void setSpringProxyClassLoader(ClassLoader classLoader) {
        springProxyClassLoader = classLoader;
    }
    
    /**
     * 1、将beanFactory的类加载器指向自定义类加载器
     * 2、将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向自定义类加载器，并记录原proxyClassLoader
     * @param moduleClassLoader
     */
    public void initBeanPostProcessor(ClassLoader moduleClassLoader) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtil.getBeanFactory();
        if (beanFactory.getBeanClassLoader() == moduleClassLoader) {
            return;
        }
        beanFactory.setBeanClassLoader(moduleClassLoader);
        
        setProxyClassLoader(getBeanPostProcessorsByProxyClassLoader(), moduleClassLoader);
    }
    
    /**
     * 获取Spring中使用proxyClassLoader的BeanPostProcessor集合对应的字段
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<BeanPostProcessor> getBeanPostProcessorsByProxyClassLoader() {
        /** 
         * SpringBoot 中JDK代理，cglib代理，初始化用的AppClassLoader加载class，外部加载的jar不在AppClassLoader范围内，从而出现 ClassNotFoundException
         * 通过反射，将容器里原proxyClassLoader指向AppClassLoader，替换为自定义ClassLoader
         * 
         **/
        List<BeanPostProcessor> beans = new ArrayList<>();
        ListableBeanFactory factory = SpringUtil.getBeanFactory();
        Field beanPostProcessorsField = FieldUtils.getField(factory.getClass(), "beanPostProcessors", true);
        if (beanPostProcessorsField != null) {
            try {
                List<BeanPostProcessor> beanPostProcessors = (List<BeanPostProcessor>) FieldUtils.readField(beanPostProcessorsField, factory, true);
                for (BeanPostProcessor processor : beanPostProcessors) {
                    if (ProxyProcessorSupport.class.isAssignableFrom(processor.getClass())) {
                        beans.add(processor);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return beans;
    }
    
    public void setProxyClassLoader(List<BeanPostProcessor> processors, ClassLoader moduleClassLoader) {
        for (BeanPostProcessor processor : processors) {
            Field field = FieldUtils.getField(processor.getClass(), "proxyClassLoader", true);
            if (field != null) {
                try {
                    ClassLoader proxyClassLoader = (ClassLoader) FieldUtils.readField(field, processor);
                    logger.info("processor----{}:{}->{}", processor.getClass().getName(), proxyClassLoader, moduleClassLoader);
                    FieldUtils.writeField(field, processor, moduleClassLoader, true);
                    
                    /** 缓存 */
                    if (springProxyClassLoader == null) {
                        setSpringProxyClassLoader(proxyClassLoader);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
