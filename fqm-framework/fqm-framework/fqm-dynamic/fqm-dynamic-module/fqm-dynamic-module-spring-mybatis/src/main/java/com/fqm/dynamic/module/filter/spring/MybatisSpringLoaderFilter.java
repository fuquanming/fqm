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

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import org.mybatis.spring.mapper.MapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.dynamic.module.filter.spring.AbstractSpringLoaderFilter;

import cn.hutool.extra.spring.SpringUtil;

/**
 * 
 * @version 
 * @author 傅泉明
 */
public class MybatisSpringLoaderFilter extends AbstractSpringLoaderFilter {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    /** 扫描的mybatis的包 */
    private String[] mapperScans;
    
    public MybatisSpringLoaderFilter(String ... mapperScans) {
        this.mapperScans = mapperScans;
    }
    
    /**
     * 判断class对象是否带有spring的注解->Repository
     */
    public boolean isSpringDaoClass(Class<?> clazz) {
        if ((clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) 
                && clazz.isAnnotationPresent(Repository.class)) {
            return true;
        }
        return false;
    }
    
    private boolean isMapperScans(String className) {
        if (mapperScans == null) return false;
        for (String mapperScan : mapperScans) {
            if (className.startsWith(mapperScan)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean loaderClassLoader(ModuleClassLoader moduleClassLoader) {
        /** 获取bean工厂并转换为DefaultListableBeanFactory */
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) SpringUtil.getBeanFactory();

        Map<String, Class<?>> classMap = moduleClassLoader.getClassMap();
        for (Entry<String, Class<?>> entry : classMap.entrySet()) {
            String className = entry.getKey();
            Class<?> clazz = entry.getValue();

            if (isSpringDaoClass(clazz) && isMapperScans(className)) {
                //将变量首字母置小写
                String beanName = StringUtils.uncapitalize(className);
                beanName = beanName.substring(beanName.lastIndexOf(".") + 1);
                beanName = StringUtils.uncapitalize(beanName);

                /** 使用spring的BeanDefinitionBuilder将Class对象转成BeanDefinition */
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition)beanDefinitionBuilder.getRawBeanDefinition();
                //设置当前bean定义对象是单利的
                beanDefinition.setScope("singleton");
                
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(clazz);
                beanDefinition.setBeanClass(MapperFactoryBean.class);
                beanDefinition.setLazyInit(false);
                /** 属性注入 */
                beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                
                /** 以指定beanName注册上面生成的BeanDefinition */
                defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinition);
                
                logger.info("loader->registerBeanDefinition=" + beanName);
            }
        }
        return true;
    }

}
