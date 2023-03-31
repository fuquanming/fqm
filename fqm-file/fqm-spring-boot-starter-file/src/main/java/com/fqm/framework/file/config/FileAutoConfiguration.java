/*
 * @(#)FileAutoConfiguration.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-file
 * 创建日期 : 2022年9月12日
 * 修改历史 : 
 *     1. [2022年9月12日]创建文件 by 傅泉明
 */
package com.fqm.framework.file.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.file.FileFactory;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
public class FileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    FileFactory fileFactory() {
        return new FileFactory();
    }

}