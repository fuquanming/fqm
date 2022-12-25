package com.fqm.framework.mq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.core.exception.GlobalException;
import com.fqm.framework.common.core.exception.enums.GlobalErrorCodeConstants;
import com.fqm.framework.mq.template.MqTemplate;


/**
 * 消息队列工厂
 * 
 * @version 
 * @author 傅泉明
 */
public class MqFactory {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, MqTemplate> mqTemplateMap = new ConcurrentHashMap<>();

    public MqFactory addMqTemplate(MqTemplate mqTemplate) {
        logger.info("Init MqTemplate->{}", mqTemplate.getClass());
        String mqTemplateName = mqTemplate.getClass().getName();
        mqTemplateMap.put(mqTemplateName, mqTemplate);
        mqTemplateMap.put(mqTemplate.getMqMode().name(), mqTemplate);
        return this;
    }
    
    private void checkMqTemplate(MqTemplate mqTemplate) {
        if (null == mqTemplate) {
            throw new GlobalException(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未注册该消息模板");
        }
    }

    public MqTemplate getMqTemplate(Class<? extends MqTemplate> mqTemplateClass) {
        MqTemplate mqTemplate = mqTemplateMap.get(mqTemplateClass.getName());
        checkMqTemplate(mqTemplate);
        return mqTemplate;
    }

    public MqTemplate getMqTemplate(MqMode mqMode) {
        if (mqMode == null) {
            checkMqTemplate(null);
        }
        MqTemplate mqTemplate = mqTemplateMap.get(mqMode.name());
        checkMqTemplate(mqTemplate);
        return mqTemplate;
    }
    
    public MqTemplate getMqTemplate(String mqMode) {
        if (mqMode == null) {
            checkMqTemplate(null);
        }
        MqTemplate mqTemplate = mqTemplateMap.get(mqMode.toUpperCase());
        checkMqTemplate(mqTemplate);
        return mqTemplate;
    }
    
    public MqTemplate getMqTemplate() {
        if (mqTemplateMap.isEmpty()) {
            checkMqTemplate(null);
        }
        return mqTemplateMap.values().iterator().next();
    }
}
