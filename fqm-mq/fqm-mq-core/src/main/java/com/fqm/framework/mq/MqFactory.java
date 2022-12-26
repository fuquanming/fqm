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
    
    private void checkMqTemplate(MqTemplate mqTemplate, String msg) {
        if (null == mqTemplate) {
            throw new GlobalException(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "未注册该消息模板," + msg);
        }
    }
    /**
     * 通过消息模板Class获取消息模板
     * @param mqTemplateClass 
     * @return
     */
    public MqTemplate getMqTemplate(Class<? extends MqTemplate> mqTemplateClass) {
        MqTemplate mqTemplate = mqTemplateMap.get(mqTemplateClass.getName());
        checkMqTemplate(mqTemplate, mqTemplateClass.getName());
        return mqTemplate;
    }
    /**
     * 通过消息模式获取消息模板
     * @param mqMode    参考 @MqMode 
     * @return
     */
    public MqTemplate getMqTemplate(MqMode mqMode) {
        if (mqMode == null) {
            throw new GlobalException(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "mqMode is null");
        }
        MqTemplate mqTemplate = mqTemplateMap.get(mqMode.name());
        checkMqTemplate(mqTemplate, mqMode.name());
        return mqTemplate;
    }
    /**
     * 通过消息模式获取消息模板
     * @param mqMode    参考 @MqMode 
     * @return
     */
    public MqTemplate getMqTemplate(String mqMode) {
        if (mqMode == null) {
            throw new GlobalException(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "mqMode is null");
        }
        String mqModeStr = mqMode.toUpperCase();
        MqTemplate mqTemplate = mqTemplateMap.get(mqModeStr);
        checkMqTemplate(mqTemplate, mqModeStr);
        return mqTemplate;
    }
    /**
     * 获取其中一个消息模板
     * @return
     */
    public MqTemplate getMqTemplate() {
        if (mqTemplateMap.isEmpty()) {
            throw new GlobalException(GlobalErrorCodeConstants.NOT_FOUND.getCode(), "MqTemplate is empty");
        }
        return mqTemplateMap.values().iterator().next();
    }
}
