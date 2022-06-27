/*
 * @(#)AbstractSpringUnloadFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring
 * 创建日期 : 2022年6月27日
 * 修改历史 : 
 *     1. [2022年6月27日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.spring.filter;

import java.util.List;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.dynamic.module.core.ModuleClassLoaderFactory;
import com.fqm.dynamic.module.filter.ModuleUnloadFilter;

import cn.hutool.extra.spring.SpringUtil;

/**
 * Spring卸载基类
 * 1、将beanFactory的类加载器指向自定义类加载器
 * 2、将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向自定义类加载器
 * 
 * @version 
 * @author 傅泉明
 */
public abstract class AbstractSpringUnloadFilter extends SpringBeanPostProcessorFilter implements ModuleUnloadFilter {

    /**
     * 加载类加载器
     * @param moduleClassLoader
     * @return
     */
    public abstract void unloadClassLoader(ModuleClassLoader moduleClassLoader);
    
    @Override
    public void unload(ModuleClassLoader moduleClassLoader) {
        init(moduleClassLoader);
        unloadClassLoader(moduleClassLoader);
        afterUnload(moduleClassLoader);
    }
    
    /**
     * 1、将beanFactory的类加载器指向自定义类加载器
     * 2、将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向自定义类加载器
     * @param moduleClassLoader
     */
    public void init(ModuleClassLoader moduleClassLoader) {
        initBeanPostProcessor(moduleClassLoader);
    }

    /**
     * 1、将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向原类加载器
     * @param moduleClassLoader
     */
    private void afterUnload(ModuleClassLoader moduleClassLoader) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) SpringUtil.getBeanFactory();
        ClassLoader threadClassLoader = ModuleClassLoaderFactory.getThreadClassLoader();
        if (beanFactory.getBeanClassLoader() == threadClassLoader) {
            return;
        }
        beanFactory.setBeanClassLoader(threadClassLoader);
        
        List<BeanPostProcessor> processors = getBeanPostProcessorsByProxyClassLoader();
        setProxyClassLoader(processors, SPRING_PROXY_CLASSLOADER);
    }
}
