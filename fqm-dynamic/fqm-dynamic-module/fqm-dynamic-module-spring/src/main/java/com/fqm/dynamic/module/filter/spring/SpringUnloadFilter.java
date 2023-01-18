/*
 * @(#)SpringUnloadFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring
 * 创建日期 : 2022年6月21日
 * 修改历史 : 
 *     1. [2022年6月21日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.spring;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.framework.common.spring.util.SpringUtil;

/**
 * Spring 卸载过滤器
 * @version 
 * @author 傅泉明
 */
public class SpringUnloadFilter extends AbstractSpringUnloadFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void unloadClassLoader(ModuleClassLoader moduleClassLoader) {
        /** 获取bean工厂并转换为DefaultListableBeanFactory */
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) SpringUtil.getBeanFactory();

        Map<String, Class<?>> classMap = moduleClassLoader.getClassMap();

        /** 卸载Controller */
        final RequestMappingHandlerMapping requestMappingHandlerMapping = getRequestMappingHandlerMapping();
        if (requestMappingHandlerMapping != null) {
            for (Entry<String, Class<?>> entry : classMap.entrySet()) {
                String className = entry.getKey();
                try {
                    Class<?> targetClass = ClassUtils.forName(className, moduleClassLoader);
                    if (targetClass.isAnnotationPresent(Controller.class) || targetClass.isAnnotationPresent(RestController.class)) {
                        unloadRequestMapping(requestMappingHandlerMapping, className, targetClass);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }

        for (Entry<String, Class<?>> entry : classMap.entrySet()) {
            String className = entry.getKey();

            //将变量首字母置小写
            String beanName = StringUtils.uncapitalize(className);
            beanName = beanName.substring(beanName.lastIndexOf(".") + 1);
            beanName = StringUtils.uncapitalize(beanName);

            unloadBeanCache(defaultListableBeanFactory, beanName);
        }
    }
    
    /**
     * 卸载Bean缓存
     * @param defaultListableBeanFactory
     * @param beanName
     */
    @SuppressWarnings("unchecked")
    private void unloadBeanCache(DefaultListableBeanFactory defaultListableBeanFactory, String beanName) {
        /** 已经在spring容器就删了 */
        if (defaultListableBeanFactory.containsBeanDefinition(beanName)) {
            defaultListableBeanFactory.removeBeanDefinition(beanName);
            try {
                // 重点：删除以前的合并的bean,removeBeanDefinition 只是设置标志位，没有删除,会在新的jar定义时引用旧的jar里的bean
                Map<String, RootBeanDefinition> mergedBeanDefinitions = (Map<String, RootBeanDefinition>)FieldUtils.readField(defaultListableBeanFactory, "mergedBeanDefinitions", true);
                mergedBeanDefinitions.remove(beanName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            logger.info("unload->removeBeanDefinition={}", beanName);
        }
    }
    /**
     * 卸载Controller
     * @param requestMappingHandlerMapping
     * @param className
     * @param targetClass
     */
    private void unloadRequestMapping(final RequestMappingHandlerMapping requestMappingHandlerMapping, String className, Class<?> targetClass) {
        try {
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
    }

}
