/*
 * @(#)SwaggerDynamicProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-swagger
 * 创建日期 : 2022年12月8日
 * 修改历史 : 
 *     1. [2022年12月8日]创建文件 by 傅泉明
 */
package com.fqm.framework.swagger.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 配置文件，动态修改
 * 1、关闭 swagger
 * 1）未配置 swagger.enabled        触发
 * 2）配置 swagger.enabled=false   触发
 * 参考 OpenApiAutoConfiguration，swagger 关闭配置：
 *  springfox:
      documentation:
        enabled: false
        auto-startup: false
        swagger-ui:
          enabled: false
        open-api:
          enabled: false
 * 2、关闭 knife4j
 * 1）加载 ProductionSecurityFilter，参见：Swagger3AutoConfiguration.productionSecurityFilter()
 * 2）不使用配置 knife4j.enable=true 且 knife4j.production=true，才能生效，knife4j.enable=true需要swagger类，已配置swagger关闭，不会有swagger类，导致找不到类。
 * 3）因此不用加载配置文件，使用加载类的方式关闭 knife4j。源码配置类：Knife4jAutoConfiguration
 * @version 
 * @author 傅泉明
 */
public class SwaggerDynamicProperties implements EnvironmentPostProcessor {

    @SuppressWarnings("unchecked")
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Iterator<PropertySource<?>> it = propertySources.iterator();
        String findPropertySourceName = null;
        
        String springfoxDocumentationEnabledStr = "springfox.documentation.enabled";
        String springfoxDocumentationAutostartupStr = "springfox.documentation.auto-startup";
        String springfoxDocumentationSwaggeruiEnabledStr = "springfox.documentation.swagger-ui.enabled";
        String springfoxDocumentationOpenapiEnabledStr = "springfox.documentation.open-api.enabled";
        
        String knife4jEnabledStr = "knife4j.enable";
        // 是否开启 Swagger
        boolean enabled = false;
        while (it.hasNext()) {
            PropertySource<?> propertySource = it.next();
            findPropertySourceName = propertySource.getName();
            Object swaggerEnabled = propertySource.getProperty("swagger.enabled");
            // 有配置时 propertySource的Name=configurationProperties 和 yml（自定义） 都会有该属性，即有2个 propertySource 命中
            if (null != swaggerEnabled && Boolean.valueOf(swaggerEnabled.toString())) {
                enabled = true;
            }
            // 清除 springfox.documentation 配置
            boolean springfoxEnabledFlag = propertySource.containsProperty(springfoxDocumentationEnabledStr);
            boolean springfoxAutostartupFlag = propertySource.containsProperty(springfoxDocumentationAutostartupStr);
            boolean springfoxSwaggeruiEnabledFlag = propertySource.containsProperty(springfoxDocumentationSwaggeruiEnabledStr);
            boolean springfoxOpenapiEnabledFlag = propertySource.containsProperty(springfoxDocumentationOpenapiEnabledStr);
            // 清除 knife4j 配置
            boolean knife4jEnabledFlag = propertySource.containsProperty(knife4jEnabledStr);
            if (springfoxEnabledFlag || springfoxAutostartupFlag || springfoxSwaggeruiEnabledFlag 
                    || springfoxOpenapiEnabledFlag || knife4jEnabledFlag) {
                Object source = propertySource.getSource();
                if (source instanceof Map) {
                    Map<String, Object> activeSource = (Map<String, Object>) propertySource.getSource();
                    Map<String, Object> newConfigMap = new HashMap<>(activeSource.size());
                    activeSource.forEach((k, v) -> newConfigMap.put(k, v.toString()));
                    newConfigMap.remove(springfoxDocumentationEnabledStr);
                    newConfigMap.remove(springfoxDocumentationAutostartupStr);
                    newConfigMap.remove(springfoxDocumentationSwaggeruiEnabledStr);
                    newConfigMap.remove(springfoxDocumentationOpenapiEnabledStr);
                    
                    newConfigMap.remove(knife4jEnabledStr);
                    propertySources.replace(propertySource.getName(), new MapPropertySource(propertySource.getName(), newConfigMap));
                }
            }
        }
        
        // 关闭 Swagger
        if (!enabled && null != findPropertySourceName && propertySources.get(findPropertySourceName).getSource() instanceof Map) {
            // 找到最后一个配置
            Map<String, Object> activeSource = (Map<String, Object>) propertySources.get(findPropertySourceName).getSource();
            Map<String, Object> newConfigMap = new HashMap<>(activeSource.size() + 4);
            // value必须要放入String格式
            activeSource.forEach((k, v) -> newConfigMap.put(k, v.toString()));
            String closeStr = "false";
            // swagger 关闭
            newConfigMap.put(springfoxDocumentationEnabledStr, closeStr);
            newConfigMap.put(springfoxDocumentationAutostartupStr, closeStr);
            newConfigMap.put(springfoxDocumentationSwaggeruiEnabledStr, closeStr);
            newConfigMap.put(springfoxDocumentationOpenapiEnabledStr, closeStr);
            propertySources.replace(findPropertySourceName, new MapPropertySource(findPropertySourceName, newConfigMap));
        }
    }

}
