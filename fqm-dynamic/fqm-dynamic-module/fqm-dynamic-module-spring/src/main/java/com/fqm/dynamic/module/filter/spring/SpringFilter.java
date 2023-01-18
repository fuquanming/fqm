/*
 * @(#)SpringFilter.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-dynamic-module-spring
 * 创建日期 : 2023年1月16日
 * 修改历史 : 
 *     1. [2023年1月16日]创建文件 by 傅泉明
 */
package com.fqm.dynamic.module.filter.spring;

import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fqm.framework.common.spring.util.SpringUtil;

/**
 * Spring 过滤器
 * 1、获取RequestMappingHandlerMapping
 * @version 
 * @author 傅泉明
 */
public interface SpringFilter {

    /**
     * 获取 RequestMappingHandlerMapping
     * @return
     */
    public default RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        Map<String, RequestMappingHandlerMapping> mappings = SpringUtil.getBeansOfType(RequestMappingHandlerMapping.class);
        RequestMappingHandlerMapping handlerMapping = null;
        
        for (RequestMappingHandlerMapping mapping : mappings.values()) {
            if (mapping.getClass() == RequestMappingHandlerMapping.class) {
                handlerMapping = mapping;
                break;
            }
        }
        return handlerMapping;
    }
    
}
