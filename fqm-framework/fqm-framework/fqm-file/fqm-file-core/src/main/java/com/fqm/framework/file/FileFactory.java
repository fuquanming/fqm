package com.fqm.framework.file;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fqm.framework.file.template.FileTemplate;


/**
 * 文件工厂
 * 
 * @version 
 * @author 傅泉明
 */
public class FileFactory {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, FileTemplate> fileTemplateMap = new ConcurrentHashMap<>();

    public FileFactory addFileTemplate(FileTemplate fileTemplate) {
        logger.info("Init FileTemplate->{}", fileTemplate.getClass());
        String fileTemplateName = fileTemplate.getClass().getName();
        fileTemplateMap.put(fileTemplateName, fileTemplate);
        fileTemplateMap.put(fileTemplate.getFileMode().name(), fileTemplate);
        return this;
    }

    public FileTemplate getFileTemplate(Class<? extends FileTemplate> fileTemplateClass) {
        return fileTemplateMap.get(fileTemplateClass.getName());
    }

    public FileTemplate getFileTemplate(FileMode fileMode) {
        if (fileMode == null) return null;
        return fileTemplateMap.get(fileMode.name());
    }
    
    public FileTemplate getFileTemplate(String fileMode) {
        if (fileMode == null) return null;
        return fileTemplateMap.get(fileMode);
    }
    
    public FileTemplate getFileTemplate() {
        return fileTemplateMap.isEmpty() ? null : fileTemplateMap.values().iterator().next();
    }
}
