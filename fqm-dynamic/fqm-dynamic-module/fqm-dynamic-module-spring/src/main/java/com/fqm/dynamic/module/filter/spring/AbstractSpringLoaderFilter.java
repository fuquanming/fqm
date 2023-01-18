package com.fqm.dynamic.module.filter.spring;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.dynamic.module.filter.ModuleLoaderFilter;

/**
 * Spring加载基类
 * 1、将beanFactory的类加载器指向自定义类加载器
 * 2、将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向自定义类加载器
 * 
 * @version 
 * @author 傅泉明
 */
public abstract class AbstractSpringLoaderFilter extends SpringBeanPostProcessorFilter implements ModuleLoaderFilter, SpringFilter {
    
    @Override
    public boolean loader(ModuleClassLoader moduleClassLoader) {
        init(moduleClassLoader);
        return loaderClassLoader(moduleClassLoader);
    }
    
    /**
     * 1、将beanFactory的类加载器指向自定义类加载器
     * 2、将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向自定义类加载器，并记录原proxyClassLoader
     * @param moduleClassLoader
     */
    public void init(ModuleClassLoader moduleClassLoader) {
        initBeanPostProcessor(moduleClassLoader);
    }
    /**
     * 加载类加载器
     * @param moduleClassLoader
     * @return
     */
    public abstract boolean loaderClassLoader(ModuleClassLoader moduleClassLoader);
    
}
