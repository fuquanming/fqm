package com.fqm.framework.swagger.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * @version 
 * @author 傅泉明
 */
@ConfigurationProperties("swagger")
public class SwaggerProperties {

    private String title = "接口文档";
    private String description = "本文档描述了接口定义";
    private String version = "1.0.0";
    private String basePackage;
    /** 
     * 显示api接口的路径,分隔
     * /api/.*,/manage/.*
     **/
    private String path;
    
    private List<String> pathList;
    
    private String contactName = "fqm";
    private String contactUrl = "http://www.fqm.com/";
    private String contactEmail = "67837343@qq.com";

    public String getTitle() {
        return title;
    }

    public SwaggerProperties setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SwaggerProperties setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SwaggerProperties setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public SwaggerProperties setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }
    

    public String getPath() {
        return path;
    }

    public SwaggerProperties setPath(String path) {
        this.path = path;
        if (this.path != null && !"".equals(this.path)) {
            this.pathList = new ArrayList<>();
            this.pathList = Arrays.asList(this.path.split(","));
        }
        return this;
    }

    public List<String> getPathList() {
        return pathList;
    }

    public void setPathList(List<String> pathList) {
        this.pathList = pathList;
    }

    public String getContactName() {
        return contactName;
    }

    public SwaggerProperties setContactName(String contactName) {
        this.contactName = contactName;
        return this;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public SwaggerProperties setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
        return this;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public SwaggerProperties setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
        return this;
    }
}
