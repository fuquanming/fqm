/*
 * @(#)SwaggerSpringUnLoaderFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring-swagger
 * 创建日期 : 2022年12月13日
 * 修改历史 : 
 *     1. [2022年12月13日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.spring;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.dynamic.module.core.ModuleClassLoader;

import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

/**
 * Swagger UnLoader
 * @version 
 * @author 傅泉明
 */
public class SwaggerSpringUnLoaderFilter extends AbstractSpringUnloadFilter implements SwaggerSpringFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /** 分组名称 */
    private String groupName;

    /**
     * 构造器
     * @param groupName     分组名称
     */
    public SwaggerSpringUnLoaderFilter(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public void unloadClassLoader(ModuleClassLoader moduleClassLoader) {
        /** swagger解析文档的入口类 */
        DocumentationPluginsBootstrapper bootstrapper = getDocumentBootstrapper();
        try {
            List<DocumentationPlugin> documentationPlugins = getDocumentationPlugins(bootstrapper);
            for (Iterator<DocumentationPlugin> it = documentationPlugins.iterator(); it.hasNext();) {
                DocumentationPlugin documentationPlugin = it.next();
                if (documentationPlugin.getGroupName().equals(groupName)) {
                    it.remove();
                    logger.info("unload->remove Swagger Docker GroupName={}", groupName);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
