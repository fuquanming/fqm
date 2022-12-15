package com.fqm.framework.swagger.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fqm.framework.common.swagger.DocketBuilder;
import com.fqm.framework.common.swagger.config.SwaggerProperties;
import com.github.xiaoymin.knife4j.spring.filter.ProductionSecurityFilter;

import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger3、Knife4j 自动装配
 * io.swagger.v3.oas.annotations.tags.Tag(在controller上注解不能在ui上显示，用 @Api 替换), @Operation 
 * 1、OpenApiAutoConfiguration，访问路径：/swagger-ui/index.html
 * 2、Knife4jAutoConfiguration，访问路径：/doc.html
 * @version 
 * @author 傅泉明
 */
@Configuration
@EnableConfigurationProperties({ SwaggerProperties.class })
public class Swagger3AutoConfiguration {

    /**
     * 加载 Swagger 
     * 配置 swagger.enabled=true 触发
     * @param swaggerProperties
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "swagger.enabled", havingValue = "true")
    public Docket createAppDocket(SwaggerProperties swaggerProperties) {
        return new DocketBuilder(swaggerProperties).build();
    }
    
    /**
     * 关闭 knife4j，参考 Knife4jAutoConfiguration
     * 1、配置 swagger 
     *  1）swagger.enabled=false   触发
     *  2）未配置 swagger.enabled   触发
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "swagger.enabled", havingValue = "false", matchIfMissing = true)
    public ProductionSecurityFilter productionSecurityFilter() {
        return new ProductionSecurityFilter(true);
    }

}
