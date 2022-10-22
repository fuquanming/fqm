package com.fqm.framework.mq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public MqTemplate getMqTemplate(Class<? extends MqTemplate> mqTemplateClass) {
        return mqTemplateMap.get(mqTemplateClass.getName());
    }

    public MqTemplate getMqTemplate(MqMode mqMode) {
        if (mqMode == null) {
            return null;
        }
        return mqTemplateMap.get(mqMode.name());
    }
    
    public MqTemplate getMqTemplate(String mqMode) {
        if (mqMode == null) {
            return null;
        }
        return mqTemplateMap.get(mqMode.toUpperCase());
    }
    
    public MqTemplate getMqTemplate() {
        return mqTemplateMap.isEmpty() ? null : mqTemplateMap.values().iterator().next();
    }
}
