package com.fqm.framework.swagger.config;

import com.fqm.framework.common.swagger.DocketBuilder;
import com.fqm.framework.common.swagger.config.SwaggerProperties;
import com.github.xiaoymin.knife4j.spring.filter.ProductionSecurityFilter;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Swagger3、Knife4j 自动装配
 * io.swagger.v3.oas.annotations.tags.Tag(在controller上注解不能在ui上显示，用 @Api 替换), @Operation 
 * 1、OpenApiAutoConfiguration，访问路径：/swagger-ui/index.html
 * 2、Knife4jAutoConfiguration，访问路径：/doc.html
 * 
 * SpringBoot 2.6 及以上和 Swagger3.0不兼容：
 *  springfox.documentation.spring.web.WebMvcPatternsRequestConditionWrapper.getPatterns 方法中 this.condition 为 null
 *  ->RequestMappingInfo.patternsCondition 为 null
 *  ->RequestMappingHandlerMapping.initLookupPath(request)【AbstractHandlerMethodMapping.getHandlerInternal->initLookupPath】，中attributes移除了 UrlPathHelper.PATH_ATTRIBUTE
 * 解决：
 * 1）spring.mvc.pathmatch.matching-strategy=ant_path_matcher 修改配置
 * 2）拦截 SpringFox 注入Bean->WebMvcRequestHandlerProvider
 * ->handlerMappings->getHandlerMethods->RequestMappingInfo->patternsCondition 赋值
 * @version 
 * @author 傅泉明
 */
@Configuration
@EnableConfigurationProperties({ SwaggerProperties.class })
public class Swagger3AutoConfiguration extends ApplicationObjectSupport {

    /**
     * 加载 Swagger 
     * 配置 swagger.enabled=true 触发
     * @param swaggerProperties
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "swagger.enabled", havingValue = "true")
    Docket createAppDocket(SwaggerProperties swaggerProperties) {
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
    ProductionSecurityFilter productionSecurityFilter() {
        return new ProductionSecurityFilter(true);
    }

    /**
     * Spring Boot 2.6 与 swagger3
     */
    @Bean
    @ConditionalOnClass(WebMvcRequestHandlerProvider.class)
    @ConditionalOnProperty(name = "swagger.enabled", havingValue = "true")
    BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @SuppressWarnings("unchecked")
            @Override
            public Object postProcessAfterInitialization(@Nonnull
            Object bean, @Nonnull
            String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    try {
                        List<RequestMappingInfoHandlerMapping> mappings = (List<RequestMappingInfoHandlerMapping>) FieldUtils
                                .readField(bean, "handlerMappings", true);
                        autoSpringfoxHandlerMappings(mappings);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void autoSpringfoxHandlerMappings(List<T> mappings) throws IllegalAccessException {
                for (RequestMappingInfoHandlerMapping mapping : mappings) {
                    Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
                    for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : map.entrySet()) {
                        RequestMappingInfo key = entry.getKey();
                        PathPatternsRequestCondition pathPatterns = key.getPathPatternsCondition();
                        if (null != pathPatterns) {
                            PathMatcher pathMatcher = new AntPathMatcher(pathPatterns.getFirstPattern().toString());
                            // 赋值 patternsCondition
                            PatternsRequestCondition patherns = new PatternsRequestCondition(
                                    new String[] { pathPatterns.getFirstPattern().toString() }, true, pathMatcher);
                            FieldUtils.writeDeclaredField(key, "patternsCondition", patherns, true);
                        }
                    }
                }
            }
        };
    }
}
