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
        logger.info("init MqTemplate->{}", mqTemplate.getClass());
        String mqTemplateName = mqTemplate.getClass().getName();
        mqTemplateMap.put(mqTemplateName, mqTemplate);

        String mqName = mqTemplate.getClass().getSimpleName().toLowerCase();
        if (mqName.contains(MqMode.kafka.name().toLowerCase())) {
            mqTemplateMap.put(MqMode.kafka.name(), mqTemplate);
        } else if (mqName.contains(MqMode.rabbit.name().toLowerCase())) {
            mqTemplateMap.put(MqMode.rabbit.name(), mqTemplate);
        } else if (mqName.contains(MqMode.redisson.name().toLowerCase())) {
            mqTemplateMap.put(MqMode.redisson.name(), mqTemplate);
        } else if (mqName.contains(MqMode.redis.name().toLowerCase())) {
            mqTemplateMap.put(MqMode.redis.name(), mqTemplate);
        } else if (mqName.contains(MqMode.rocket.name().toLowerCase())) {
            mqTemplateMap.put(MqMode.rocket.name(), mqTemplate);
        } else if (mqName.contains(MqMode.zookeeper.name().toLowerCase())) {
            mqTemplateMap.put(MqMode.zookeeper.name(), mqTemplate);
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
    
    public MqTemplate getMqTemplate() {
        return mqTemplateMap.isEmpty() ? null : mqTemplateMap.values().iterator().next();
    }
}
