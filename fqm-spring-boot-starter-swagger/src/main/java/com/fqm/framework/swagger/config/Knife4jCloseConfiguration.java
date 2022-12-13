/*
 * @(#)Knife4jCloseConfiguration.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-swagger
 * 创建日期 : 2022年12月9日
 * 修改历史 : 
 *     1. [2022年12月9日]创建文件 by 傅泉明
 */
package com.fqm.framework.swagger.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.xiaoymin.knife4j.spring.filter.ProductionSecurityFilter;

/**
 * Knife4j 关闭自动装配
 * @version 
 * @author 傅泉明
 */
@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "false", matchIfMissing = true)
public class Knife4jCloseConfiguration {
    /**
     * 关闭 knife4j，参考 Knife4jAutoConfiguration
     * 1、配置 swagger 
     *  1）swagger.enabled=false   触发
     *  2）未配置 swagger.enabled   触发
     * @return
     */
    @Bean
    public ProductionSecurityFilter productionSecurityFilter() {
        return new ProductionSecurityFilter(true);
    }
}
