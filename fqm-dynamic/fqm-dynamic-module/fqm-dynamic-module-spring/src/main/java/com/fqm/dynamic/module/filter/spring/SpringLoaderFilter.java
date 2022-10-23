/*
 * @(#)SpringLoaderFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring
 * 创建日期 : 2022年6月21日
 * 修改历史 : 
 *     1. [2022年6月21日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.spring;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.framework.common.spring.util.SpringUtil;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class SpringLoaderFilter extends AbstractSpringLoaderFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 判断class对象是否带有spring的注解
     */
    public boolean isSpringBeanClass(Class<?> cla) {
        /**
         * 如果为空或是接口或是抽象类直接返回false
         */
        if (cla == null || cla.isInterface() || Modifier.isAbstract(cla.getModifiers())) {
            return false;
        }

        Class<?> targetClass = cla;
        while (targetClass != null) {
            /**
             * 如果包含spring注解则返回true
             */
            if (targetClass.isAnnotationPresent(Component.class) || targetClass.isAnnotationPresent(Repository.class)
                    || targetClass.isAnnotationPresent(Service.class) || targetClass.isAnnotationPresent(Configuration.class)
                    || targetClass.isAnnotationPresent(Controller.class) || targetClass.isAnnotationPresent(RestController.class)) {
                return true;
            }
            targetClass = targetClass.getSuperclass();
        }

        return false;
    }
    
    @Override
    public boolean loaderClassLoader(ModuleClassLoader moduleClassLoader) {
        /** 获取bean工厂并转换为DefaultListableBeanFactory */
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) SpringUtil.getBeanFactory();
        Map<String, Class<?>> classMap = moduleClassLoader.getClassMap();
        Set<String> controllerNameSet = new HashSet<>();
        for (Entry<String, Class<?>> entry : classMap.entrySet()) {
            String className = entry.getKey();
            Class<?> clazz = entry.getValue();

            if (isSpringBeanClass(clazz)) {
                //将变量首字母置小写
                String beanName = StringUtils.uncapitalize(className);
                beanName = beanName.substring(beanName.lastIndexOf(".") + 1);
                beanName = StringUtils.uncapitalize(beanName);

                /** 使用spring的BeanDefinitionBuilder将Class对象转成BeanDefinition */
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition)beanDefinitionBuilder.getRawBeanDefinition();
                //设置当前bean定义对象是单利的
                beanDefinition.setScope("singleton");
                beanDefinition.setLazyInit(false);
                beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                /** 以指定beanName注册上面生成的BeanDefinition */
                defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinition);
                
                logger.info("loader->registerBeanDefinition={}", beanName);
                
                if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                    controllerNameSet.add(beanName);
                }
            }
        }

        /** 注册Controller */
        final RequestMappingHandlerMapping requestMappingHandlerMapping = SpringUtil.getBean(RequestMappingHandlerMapping.class);
        if (requestMappingHandlerMapping != null) {
            for (String beanName : controllerNameSet) {
                initController(moduleClassLoader, defaultListableBeanFactory, requestMappingHandlerMapping, beanName);
            }
        }
        return true;
    }

    private void initController(ModuleClassLoader moduleClassLoader, DefaultListableBeanFactory defaultListableBeanFactory,
            final RequestMappingHandlerMapping requestMappingHandlerMapping, String beanName) throws LinkageError {
        boolean controllerFlag = defaultListableBeanFactory.containsBean(beanName);
        if (controllerFlag) {
            /**
             *  卸载Controller，重点，前面registerBeanDefinition后必须执行卸载，避免生成的类和方法不匹配，
             *  SpringUnloadFilter已经卸载成功（是否执行不重要了），但是必须在该类ClassLoader中再执行一次
             */
            try {
                String className = defaultListableBeanFactory.getBean(beanName).getClass().getName();
                Class<?> targetClass = ClassUtils.forName(className, moduleClassLoader);
                ReflectionUtils.doWithMethods(targetClass, method -> {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    try {
                        RequestMappingInfo requestMappingInfo = (RequestMappingInfo) MethodUtils.invokeMethod(
                                requestMappingHandlerMapping, true, "getMappingForMethod", specificMethod, targetClass);
                        
                        if (requestMappingInfo != null) {
                            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                            logger.info("unload->deleteController={}", className);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, ReflectionUtils.USER_DECLARED_METHODS);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                MethodUtils.invokeMethod(requestMappingHandlerMapping, true, "detectHandlerMethods", beanName);
                logger.info("loader->addController={}", beanName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
