/*
 * @(#)ModuleClassLoader.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-core
 * 创建日期 : 2022年6月21日
 * 修改历史 : 
 *     1. [2022年6月21日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.dynamic.module.filter.ModuleLoaderFilter;
import com.fqm.dynamic.module.filter.ModuleUnloadFilter;

/**
 * 模块加载器：动态加载jar
 * 1、获取class
 * 2、获取资源文件
 * @version 
 * @author 傅泉明
 */
public class ModuleClassLoader extends URLClassLoader {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /** 加载的jar */
    private JarFile jarFile;
    /** 加载的模块名称 */
    private String moduleName;

    private Map<String, byte[]> classByteMap = new HashMap<>();

    private Map<String, Class<?>> classMap = new HashMap<>();
    /** META-INF文件 */
    private Map<String, byte[]> metaInfMap = new HashMap<>();
    /** 其他文件 */
    private Map<String, byte[]> fileMap = new HashMap<>();
    
    private List<ModuleLoaderFilter> loaderFilters; 
    private List<ModuleUnloadFilter> unloadFilters;

    public ModuleClassLoader(URL[] urls, ClassLoader parent, String moduleName, List<ModuleLoaderFilter> loaderFilters,
            List<ModuleUnloadFilter> unloadFilters) {
        super(urls, parent);
        logger.info("loader->parent={}", parent);
        URL url = urls[0];
        this.moduleName = moduleName;
        try {
            jarFile = new JarFile(url.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.loaderFilters = loaderFilters;
        this.unloadFilters = unloadFilters;

    }

    /**
     * 加载jar文件，解析class，META-INF，及其他文件
     */
    public void init() {
        /** 初始化必须用自己的类加载器 */
        Thread.currentThread().setContextClassLoader(this);
        
        //解析jar包每一项
        Enumeration<JarEntry> en = jarFile.entries();
        /** class文件后缀 */
        String classSuffix = ".class";
        /** META-INF文件前缀 */
        String metaInfPrefix = "META-INF";
        
        while (en.hasMoreElements()) {
            JarEntry je = en.nextElement();
            String name = je.getName();
            int bufferSize = 4096;
            byte[] bytes = null;
            try (InputStream input = jarFile.getInputStream(je); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[bufferSize];
                int bytesNumRead = 0;
                while ((bytesNumRead = input.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesNumRead);
                }
                bytes = baos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
            /** 过滤class */
            if (name.endsWith(classSuffix)) {
                String className = name.replace(classSuffix, "").replace("/", ".");
                classByteMap.put(className, bytes);
            } else if (name.startsWith(metaInfPrefix)) {
                /** 过滤META-INF */
                metaInfMap.put(name, bytes);
            } else {
                fileMap.put(name, bytes);
            }
        }
        try {
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initClass();
    }

    private void initClass() {
        /** 将jar中的class字节码进行Class载入 */
        for (Map.Entry<String, byte[]> entry : classByteMap.entrySet()) {
            String key = entry.getKey();
            Class<?> clazz = null;
            try {
                clazz = loadClass(key);
            } catch (ClassNotFoundException e) {
                logger.error("jarFile=" + jarFile.getName() + ",loadClass error", e);
            }
            classMap.put(key, clazz);
            
        }
        if (loaderFilters != null) {
            for (ModuleLoaderFilter filter : loaderFilters) {
                boolean flag = filter.loader(this);
                if (!flag) {
                    logger.error("moduleLoader,name={},loaderFilter error={}", moduleName, filter);
                    break;
                }
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classMap.containsKey(name)) {
            logger.debug("findClass from classMap:{}", name);
            return classMap.get(name);
        }
        
        logger.debug("findClass:{}", name);
        byte[] bytes = classByteMap.get(name);
        if (bytes == null) {
            return super.findClass(name);
        }

        /** 使用AppClassLoader 加载 */
        Object[] args = new Object[] { name, bytes, 0, bytes.length };
        ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        /** 
         * SpringBoot 中JDK代理，cglib代理，初始化用的AppClassLoader加载class，外部加载的jar不在AppClassLoader范围内，从而出现 ClassNotFoundException
         * 通过反射，调用AppClassLoader.defineClass 来加载class->缺点：无法替换AppClassLoader已经加载的class
         * 使用自定义加载，通过替换Spring容器里的AppClassLoader地址实现
         * 
         **/
        try {
            Class<?> clazz = (Class<?>) MethodUtils.invokeMethod(appClassLoader, true, "defineClass", args);
            logger.debug("findClass->appClassLoader defineClass:{}", name);
            return clazz;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.findClass(name);
    }
    
    public void unload() {
        if (unloadFilters != null) {
            /** 卸载必须用自己的类加载器 */
            Thread.currentThread().setContextClassLoader(this);
            for (ModuleUnloadFilter filter : unloadFilters) {
                try {
                    filter.unload(this);
                } catch (Exception e) {
                    logger.error("moduleLoader,name=" + moduleName + ",unloadFilter error=" + filter, e);
                }
            }
            this.classMap.clear();
            this.classByteMap.clear();
            this.metaInfMap.clear();
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    public Map<String, byte[]> getMetaInfMap() {
        return metaInfMap;
    }

    public Map<String, byte[]> getFileMap() {
        return fileMap;
    }

    public Map<String, Class<?>> getClassMap() {
        return classMap;
    }

}
