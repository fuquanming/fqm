/*
 * @(#)SwaggerSpringLoaderFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring-swagger
 * 创建日期 : 2022年12月13日
 * 修改历史 : 
 *     1. [2022年12月13日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.spring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.dynamic.module.core.ModuleClassLoader;
import com.fqm.framework.common.swagger.DocketBuilder;
import com.fqm.framework.common.swagger.config.SwaggerProperties;

import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

/**
 * Swagger Loader
 * @version 
 * @author 傅泉明
 */
public class SwaggerSpringLoaderFilter extends AbstractSpringLoaderFilter implements SwaggerSpringFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private SwaggerProperties swaggerProperties;

    /**
     * 构造器
     * @param basePackage   扫描的包名
     * @param groupName     分组名称
     */
    public SwaggerSpringLoaderFilter(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Override
    public boolean loaderClassLoader(ModuleClassLoader moduleClassLoader) {
        /** Sswagger 解析文档的入口类 */
        DocumentationPluginsBootstrapper bootstrapper = getDocumentBootstrapper();
        try {
            List<DocumentationPlugin> documentationPlugins = getDocumentationPlugins(bootstrapper);
            /** 新增 Docker */
            documentationPlugins.add(new DocketBuilder(swaggerProperties).build());
            logger.info("loader->add Swagger Docker GroupName={}", swaggerProperties.getGroupName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        /** 清空 DocumentationCache 缓存 */
        bootstrapper.stop();
        /** 手动执行重新解析 swagger 文档 */
        bootstrapper.start();
        return true;
    }

}
