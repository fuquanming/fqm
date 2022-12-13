package com.fqm.framework.common.swagger.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Swagger 配置文件
 * @version 
 * @author 傅泉明
 */
@ConfigurationProperties("swagger")
public class SwaggerProperties {

    /** 是否开启，默认为 false 未开启 */
    private Boolean enabled = false;
    /** 接口文档标题 */
    private String title = "接口文档";
    /** 接口文档描述 */
    private String description = "本文档描述了接口定义";
    /** 接口文档版本号 */
    private String version = "1.0.0";
    /** 接口文档分组名 */
    private String groupName = "default";
    /** 解析的包路径 */
    private String basePackage = "";
    /** 解析的url规则 */
    private List<String> basePath = new ArrayList<>();
    /** 在basePath基础上需要排除的url规则 */
    private List<String> excludePath = new ArrayList<>();
    /** 许可证 */
    private String license = "";
    /** 许可证URL */
    private String licenseUrl = "";
    /** 服务条款URL */
    private String termsOfServiceUrl = "";
    /** 联系人信息 */
    private Contact contact = new Contact();

    public static class Contact {
        /** 联系人 */
        private String name = "";

        /** 联系人url */
        private String url = "";

        /** 联系人email */
        private String email = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<String> getBasePath() {
        return basePath;
    }

    public void setBasePath(List<String> basePath) {
        this.basePath = basePath;
    }

    public List<String> getExcludePath() {
        return excludePath;
    }

    public void setExcludePath(List<String> excludePath) {
        this.excludePath = excludePath;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

}
