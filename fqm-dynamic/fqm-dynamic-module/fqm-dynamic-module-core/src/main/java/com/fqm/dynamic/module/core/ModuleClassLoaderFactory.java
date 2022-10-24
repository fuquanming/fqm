/*
 * @(#)ModuleClassLoaderFactory.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-core
 * 创建日期 : 2022年6月21日
 * 修改历史 : 
 *     1. [2022年6月21日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.dynamic.module.filter.ModuleLoaderFilter;
import com.fqm.dynamic.module.filter.ModuleUnloadFilter;

/**
 * 模块加载器工厂
 * @version 
 * @author 傅泉明
 */
public class ModuleClassLoaderFactory {

    private static Logger logger = LoggerFactory.getLogger(ModuleClassLoaderFactory.class);

    /** 缓存模块加载器 */
    private static Map<String, ModuleClassLoader> moduleClassLoaderMap = new HashMap<>();
    /** 调用ModuleClassLoaderFactory的类加载器 */
    private static ClassLoader threadClassLoader = null;
    /** 缓存加载器 */
    private static Map<Object, ClassLoader> cacheClassLoaderMap = new HashMap<>();
    
    private static Object obj = new Object();
    /** jdk 版本 */
    private static String jdk8 = "1.8.0_2";
    
    private ModuleClassLoaderFactory() {
    }
    /**
     * 加载模块
     * 1、使用自定义类加载器初始化自定义类加载器
     * 2、使用Spring容器
     *  1）将beanFactory的类加载器指向自定义类加载器
     *  2）将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向自定义类加载器
     * @param jarPath       jar包路径
     * @param moduleName    jar对应的模块名称
     * @param loaderFilters jar加载的过滤器
     * @param unloadFilters jar卸载的过滤器
     * @return
     */
    public static ModuleClassLoader build(String jarPath, String moduleName, List<ModuleLoaderFilter> loaderFilters, 
            List<ModuleUnloadFilter> unloadFilters) {
        synchronized (obj) {
            if (threadClassLoader == null) {
                threadClassLoader = Thread.currentThread().getContextClassLoader();
            }
            
            URL[] urls = null;
            ModuleClassLoader moduleClassLoader = null;
            try {
                File jar = new File(jarPath);
                URI uri = jar.toURI();
                urls = new URL[] {uri.toURL()};
                
                unloadModule(moduleName);
                
                moduleClassLoader = new ModuleClassLoader(urls, threadClassLoader, moduleName, loaderFilters, unloadFilters);
                
                moduleClassLoaderMap.put(moduleName, moduleClassLoader);
                
                logger.info("加载模块:{},{}", moduleName, moduleClassLoader);
                moduleClassLoader.init();
                logger.info("加载模块完成:{}", moduleName);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("加载模块失败:", e);
            }
            return moduleClassLoader;
        }
    }
    
    public static ModuleClassLoader getModuleClassLoader(String moduleName) {
        return moduleClassLoaderMap.get(moduleName);
    }

    /**
     * 卸载模块
     * 1、使用自定义类加载器卸载类加载器
     * 2、使用Spring容器
     *  1）将beanFactory的类加载器指向自定义类加载器
     *  2）将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都指向自定义类加载器
     *  3）卸载完成，将beanFactory属性beanPostProcessors中所有对象的属性proxyClassLoader都还原为原来的AppClassLoader
     * @param moduleName
     */
    public static void unloadModule(String moduleName) {
        synchronized (obj) {
            if (threadClassLoader == null) {
                threadClassLoader = Thread.currentThread().getContextClassLoader();
            }
            
            ModuleClassLoader moduleClassLoaderCache = moduleClassLoaderMap.get(moduleName);
            if (moduleClassLoaderCache != null) {
                logger.info("卸载模块:{},{}", moduleName, moduleClassLoaderCache);
                moduleClassLoaderCache.unload();
                moduleClassLoaderMap.remove(moduleName);
                String javaVersion = System.getProperty("java.version");
                if (null != javaVersion && javaVersion.startsWith(jdk8)) {
                    try {
                        Class<?> clazz = Class.forName("sun.misc.ClassLoaderUtil");
                        if (null != clazz) {
                            MethodUtils.invokeStaticMethod(clazz, "releaseLoader", moduleClassLoaderCache);
                        }
                    } catch (ClassNotFoundException e) {
                        // nothing
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }                    
                }
                logger.info("卸载模块完成:{}", moduleName);
            }
        }
    }

    public static ClassLoader getThreadClassLoader() {
        return threadClassLoader;
    }
    public static Map<Object, ClassLoader> getCacheClassLoaderMap() {
        return cacheClassLoaderMap;
    }
}
