package com.fqm.framework.common.spring.util;
/**
 * 解决自定义标签读取配置信息，使用@Value读取
 * @Job(cron = "${app.job.cron}")
 * 
 * 1、@Value("${app.port}") 读取配置信息
 * 2、@Value("#{userService.count(${app.name})}") 调研容器执行带参数的方法
 * 3、@Value("#{${app.size} <= '12345'.length() ? ${app.size} : '12345'.length()}") 简单运算
 * 
 * @version 
 * @author 傅泉明
 */

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;

public class ValueUtil {

    private static final BeanExpressionResolver RESSOLVER = new StandardBeanExpressionResolver();
    
    private static String elStartStr = "#{";
    private static String elEndStr = "}";
    
    private ValueUtil() {
    }
    /**
     * 解析表达式，或读取配置信息 
     * @param beanFactory
     * @param value
     * @return
     */
    public static Object resolveExpression(ConfigurableBeanFactory beanFactory, String value) {
        // 获取输入的内容，返回的内容
        String resolveValue = beanFactory.resolveEmbeddedValue(value);
        
        // 表达式
        if (null != resolveValue && resolveValue.startsWith(elStartStr) && resolveValue.endsWith(elEndStr)) {
            return RESSOLVER.evaluate(resolveValue, new BeanExpressionContext(beanFactory, null));
        }
        // 读取配置信息
        return resolveValue;
    }
}
