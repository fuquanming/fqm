/*
 * @(#)DocketBuilder.java
 * 
 * Copyright (c) 2015, All Rights Reserved
 * 项目名称 : fqm-common-swagger
 * 创建日期 : 2022年12月7日
 * 修改历史 : 
 *     1. [2022年12月7日]创建文件 by 傅泉明
 */
package com.fqm.framework.common.swagger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.util.StringUtils;

import com.fqm.framework.common.swagger.config.SwaggerProperties;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger Docket 构造者
 * @version 
 * @author 傅泉明
 */
public class DocketBuilder {
    
    private static final String BASE_PATH = "/**";
    /** swagger 配置文件 */
    private SwaggerProperties swaggerProperties;
    
    /**
     * Docket 构造器
     * @param swaggerProperties Swagger配置文件
     */
    public DocketBuilder(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    public Docket build() {
        // basePath处理
        if (swaggerProperties.getBasePath().isEmpty()) {
            swaggerProperties.getBasePath().add(BASE_PATH);
        }
        List<Predicate<String>> basePath = new ArrayList<>();
        swaggerProperties.getBasePath().forEach(path -> basePath.add(PathSelectors.ant(path)));

        List<Predicate<String>> excludePath = new ArrayList<>();
        swaggerProperties.getExcludePath().forEach(path -> excludePath.add(PathSelectors.ant(path)));

        ApiSelectorBuilder builder = new Docket(DocumentationType.OAS_30)
                .groupName(swaggerProperties.getGroupName())
                .apiInfo(apiInfo(swaggerProperties)).select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)
                        .or(RequestHandlerSelectors.withMethodAnnotation(Operation.class)));
        String basePackage = swaggerProperties.getBasePackage();
        if (!StringUtils.hasText(basePackage)) {
            builder.apis(RequestHandlerSelectors.basePackage(basePackage));
        }

        swaggerProperties.getBasePath().forEach(p -> builder.paths(PathSelectors.ant(p)));
        swaggerProperties.getExcludePath().forEach(p -> builder.paths(PathSelectors.ant(p).negate()));

        return builder.build().securitySchemes(securitySchemes()).securityContexts(securityContexts()).pathMapping("/");
    }

    /**
     * 安全模式，这里指定token通过Authorization头请求头传递
     * @return
     */
    private List<SecurityScheme> securitySchemes() {
        String authorization = "Authorization";
        List<SecurityScheme> apiKeyList = new ArrayList<>();
        apiKeyList.add(new ApiKey(authorization, authorization, "header"));
        return apiKeyList;
    }

    /**
     * 安全上下文
     * @return
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder().securityReferences(defaultAuth()).operationSelector(o -> o.requestMappingPattern().matches("/.*")).build());
        return securityContexts;
    }

    /**
     * 默认的全局鉴权策略
     * @return
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>(1);
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }

    /**
     * api描述信息 
     * @param swaggerProperties
     * @return
     */
    private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
        return new ApiInfoBuilder().title(swaggerProperties.getTitle()).description(swaggerProperties.getDescription())
                .license(swaggerProperties.getLicense()).licenseUrl(swaggerProperties.getLicenseUrl())
                .termsOfServiceUrl(swaggerProperties.getTermsOfServiceUrl())
                .contact(new Contact(swaggerProperties.getContact().getName(), swaggerProperties.getContact().getUrl(), swaggerProperties.getContact().getEmail()))
                .version(swaggerProperties.getVersion()).build();
    }

}
