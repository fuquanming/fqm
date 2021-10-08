package com.fqm.framework.swagger.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger3配置信息
 * 访问路径:/swagger-ui/index.html
 * @EnableOpenApi
 * 生产环境配置不开启Swagger3
 * springfox:
      documentation:
        swagger-ui:
          enabled: false     # 关闭swagger
 * 
 * 标签介绍：
 *  @Api：用在请求的类上，表示对类的说明
        tags="说明该类的作用，可以在UI界面上看到的注解"
        value="该参数没什么意义，在UI界面上也看到，所以不需要配置"
    
    @ApiOperation：用在请求的方法上，说明方法的用途、作用
        value="说明方法的用途、作用"
        notes="方法的备注说明"
    
    @ApiImplicitParams：用在请求的方法上，表示一组参数说明
        @ApiImplicitParam：用在@ApiImplicitParams注解中，指定一个请求参数的各个方面
            name：参数名
            value：参数的汉字说明、解释
            required：参数是否必须传
            paramType：参数放在哪个地方
                · header --> 请求参数的获取：@RequestHeader
                · query --> 请求参数的获取：@RequestParam
                · path（用于restful接口）--> 请求参数的获取：@PathVariable
                · div（不常用）
                · form（不常用）    
            dataType：参数类型，默认String，其它值dataType="Integer"       
            defaultValue：参数的默认值
    
    @ApiResponses：用在请求的方法上，表示一组响应
        @ApiResponse：用在@ApiResponses中，一般用于表达一个错误的响应信息
            code：数字，例如400
            message：信息，例如"请求参数没填好"
            response：抛出异常的类
    
    @ApiModel：用于响应类上，表示一个返回响应数据的信息
                （这种一般用在post创建的时候，使用@RequestBody这样的场景，
                请求参数无法使用@ApiImplicitParam注解进行描述的时候）
        @ApiModelProperty：用在属性上，描述响应类的属性
        
 ************************************************************************** 
        加强Swagger文档
    @EnableKnife4j
        访问路径:/doc.html
 *  接口文档配置
    knife4j:
      enable: true
      basic:
        enable: true
        username: fqm
        password: 123456 
    # 关闭knife4j文档
    knife4j:
      production: true  
 *
 * 
 * @version 
 * @author 傅泉明
 */
@Configuration
@EnableOpenApi
@EnableKnife4j
@ConditionalOnClass({Docket.class, ApiInfoBuilder.class})
//name=enabled 且值为true，时才加载，matchIfMissing=true 缺失也加载
@ConditionalOnProperty(prefix = "swagger", value = "enabled", havingValue = "true") // 允许使用 swagger.enable=false 禁用 Swagger
@EnableConfigurationProperties(SwaggerProperties.class)
public class Swagger3AutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public SwaggerProperties swaggerProperties() {
        return new SwaggerProperties();
    }
    
    @Bean
    public Docket createMallbookApi() {
        SwaggerProperties properties = swaggerProperties();
        
        ApiSelectorBuilder apiSelectorBuilder = new Docket(DocumentationType.OAS_30)
                .groupName(properties.getTitle())
                .apiInfo(apiInfo(properties))
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        if (properties.getPathList() != null) {
            for (String path : properties.getPathList()) {
                apiSelectorBuilder.paths(PathSelectors.regex(path));
            }
        } else {
            apiSelectorBuilder.paths(PathSelectors.any());
        }
        
        return apiSelectorBuilder.build();
    }

    private ApiInfo apiInfo(SwaggerProperties properties) {
        return new ApiInfoBuilder()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .version(properties.getVersion())
                .contact(new Contact(properties.getContactName(), 
                        properties.getContactUrl(), properties.getContactEmail()))
                .build();
    }

}
