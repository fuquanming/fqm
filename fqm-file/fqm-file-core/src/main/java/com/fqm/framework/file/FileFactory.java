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

    private Map<FileMode, FileTemplate> fileTemplateMap = new ConcurrentHashMap<>();

    public FileFactory addFileTemplate(FileTemplate fileTemplate) {
        logger.info("Init FileTemplate->{}", fileTemplate.getClass());
        fileTemplateMap.put(fileTemplate.getFileMode(), fileTemplate);
        return this;
    }

    public FileTemplate getFileTemplate(FileMode fileMode) {
        if (fileMode == null) {
            return null;
        }
        return fileTemplateMap.get(fileMode);
    }
    
    public FileTemplate getFileTemplate() {
        return fileTemplateMap.isEmpty() ? null : fileTemplateMap.values().iterator().next();
    }
}
