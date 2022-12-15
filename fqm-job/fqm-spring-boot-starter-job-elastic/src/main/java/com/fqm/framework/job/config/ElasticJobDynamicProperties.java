package com.fqm.framework.job.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 配置文件，动态修改
 * 1、关闭 job
 * 1）未配置 job.enabled        触发
 * 2）配置 job.enabled=false   触发
 * 参考 ElasticJobLiteAutoConfiguration，ElasticJob 关闭配置：
 * elasticjob.enabled=false
 * @version 
 * @author 傅泉明
 */
public class ElasticJobDynamicProperties implements EnvironmentPostProcessor {

    @SuppressWarnings("unchecked")
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Iterator<PropertySource<?>> it = propertySources.iterator();
        String findPropertySourceName = null;
        
        String elasticJobEnabledStr = "elasticjob.enabled";
        // 是否开启 Swagger
        boolean enabled = false;
        while (it.hasNext()) {
            PropertySource<?> propertySource = it.next();
            findPropertySourceName = propertySource.getName();
            Object jobEnabled = propertySource.getProperty("job.enabled");
            // 有配置时 propertySource的Name=configurationProperties 和 yml（自定义） 都会有该属性，即有2个 propertySource 命中
            if (null != jobEnabled && Boolean.valueOf(jobEnabled.toString())) {
                enabled = true;
            }
            // 清除 elasticjob 配置
            boolean elasticJobEnabledFlag = propertySource.containsProperty(elasticJobEnabledStr);
            if (elasticJobEnabledFlag) {
                Object source = propertySource.getSource();
                if (source instanceof Map) {
                    Map<String, Object> activeSource = (Map<String, Object>) propertySource.getSource();
                    Map<String, Object> newConfigMap = new HashMap<>(activeSource.size());
                    activeSource.forEach((k, v) -> newConfigMap.put(k, v.toString()));
                    
                    newConfigMap.remove(elasticJobEnabledStr);
                    propertySources.replace(propertySource.getName(), new MapPropertySource(propertySource.getName(), newConfigMap));
                    System.out.println("clean=" + propertySources.get(findPropertySourceName).getSource());
                }
            }
        }
        
        // 关闭 elasticjob
        if (!enabled && null != findPropertySourceName && propertySources.get(findPropertySourceName).getSource() instanceof Map) {
            // 找到最后一个配置
            Map<String, Object> activeSource = (Map<String, Object>) propertySources.get(findPropertySourceName).getSource();
            Map<String, Object> newConfigMap = new HashMap<>(activeSource.size() + 4);
            // value必须要放入String格式
            activeSource.forEach((k, v) -> newConfigMap.put(k, v.toString()));
            // elasticjob 关闭
            newConfigMap.put(elasticJobEnabledStr, "false");
            propertySources.replace(findPropertySourceName, new MapPropertySource(findPropertySourceName, newConfigMap));
            System.out.println("new=" + propertySources.get(findPropertySourceName).getSource());
        }
    }

}
