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

import com.fqm.framework.common.core.util.JsonUtil;

/**
 * Mq properties
 * @version 
 * @author 傅泉明
 */
public class MqProperties {
    
    /** 是否开启，默认为 true 开启 */
    private Boolean enabled = true;
    /** 任务配置，key：消息主题 */
    private Map<String, MqConfigurationProperties> mqs = new LinkedHashMap<>();
    
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, MqConfigurationProperties> getMqs() {
        return mqs;
    }

    public void setMqs(Map<String, MqConfigurationProperties> mqs) {
        this.mqs = mqs;
        // 设置Topic
        for (Map.Entry<String, MqConfigurationProperties> entry : this.mqs.entrySet()) {
            String topic = entry.getKey();
            MqConfigurationProperties mcp = entry.getValue();
            mcp.setTopic(topic);
        }
    }
    
}
