/*
 * @(#)MqProperties.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-spring-boot-starter-mq
 * 创建日期 : 2022年9月7日
 * 修改历史 : 
 *     1. [2022年9月7日]创建文件 by 傅泉明
 */
package com.fqm.framework.mq.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Mq properties
 * @version 
 * @author 傅泉明
 */
@ConfigurationProperties(prefix = "mq")
public class MqProperties {
    /** 任务配置 */
    private Map<String, MqConfigurationProperties> mqs = new LinkedHashMap<>();

    public Map<String, MqConfigurationProperties> getMqs() {
        return mqs;
    }

    public void setMqs(Map<String, MqConfigurationProperties> mqs) {
        this.mqs = mqs;
    }
    
}
