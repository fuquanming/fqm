/*
 * @(#)BaseSwaggerSpringFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring-swagger
 * 创建日期 : 2022年12月13日
 * 修改历史 : 
 *     1. [2022年12月13日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.spring;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.plugin.core.PluginRegistry;

import com.fqm.framework.common.spring.util.SpringUtil;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;

/**
 * Swagger Spring Filter 基类
 * @version 
 * @author 傅泉明
 */
public interface SwaggerSpringFilter {

    /**
     * 获取 Sswagger 解析文档类
     * @return
     */
    public default DocumentationPluginsBootstrapper getDocumentBootstrapper() {
        /** 获取bean工厂并转换为DefaultListableBeanFactory */
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) SpringUtil.getBeanFactory();
        /** swagger解析文档的入口类 */
        return defaultListableBeanFactory.getBean(DocumentationPluginsBootstrapper.class);
    }
    
    /**
     * 获取 Swagger 文档集合
     * @param bootstrapper      Swagger 解析文档类
     * @return
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public default List<DocumentationPlugin> getDocumentationPlugins(DocumentationPluginsBootstrapper bootstrapper) throws IllegalAccessException {
        /** 得到 documentationPluginsManager 对象 */
        DocumentationPluginsManager documentationPluginsManager = (DocumentationPluginsManager) FieldUtils.readField(bootstrapper, "documentationPluginsManager", true);
        /** 下层得到 documentationPlugins 属性 */
        PluginRegistry<DocumentationPlugin, DocumentationType> pluginRegistrys = (PluginRegistry<DocumentationPlugin, DocumentationType>) FieldUtils.readField(documentationPluginsManager, "documentationPlugins", true);
        /** 文档插件集合 */
        List<DocumentationPlugin> dockets = pluginRegistrys.getPlugins();
        
        return (List<DocumentationPlugin>) FieldUtils.readField(dockets, "list", true);
    }
    
}
