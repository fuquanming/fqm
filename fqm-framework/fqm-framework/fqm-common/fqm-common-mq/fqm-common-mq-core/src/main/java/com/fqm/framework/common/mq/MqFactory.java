package com.fqm.framework.common.mq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.common.mq.template.MqTemplate;


/**
 * 消息队列工程
 * 
 * @version 
 * @author 傅泉明
 */
public class MqFactory {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, MqTemplate> mqTemplateMap = new ConcurrentHashMap<>();

    public MqFactory addMqTemplate(MqTemplate mqTemplate) {
        logger.info("init MqTemplate->{}", mqTemplate.getClass());
        String mqTemplateName = mqTemplate.getClass().getName();
        mqTemplateMap.put(mqTemplateName, mqTemplate);

        String mqName = mqTemplate.getClass().getSimpleName().toLowerCase();
        if (mqName.contains(MqMode.redis.name().toLowerCase())) {
            mqTemplateMap.put(MqMode.redis.name(), mqTemplate);
        }

        return this;
    }

    public MqTemplate getMqTemplate(Class<? extends MqTemplate> mqTemplateClass) {
        return mqTemplateMap.get(mqTemplateClass.getName());
    }

    public MqTemplate getMqTemplate(MqMode mqMode) {
        if (mqMode == null) return null;
        return mqTemplateMap.get(mqMode.name());
    }
    
    public MqTemplate getMqTemplate(String mqMode) {
        if (mqMode == null) return null;
        return mqTemplateMap.get(mqMode);
    }
}
