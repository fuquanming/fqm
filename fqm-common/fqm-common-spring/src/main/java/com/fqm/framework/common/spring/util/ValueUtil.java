package com.fqm.framework.common.spring.util;

import java.lang.reflect.Method;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * 自定义标签:方法参数+当前对象字段+读取配置信息+Bean方法调用
 * 方法参数     #param.name                         ✅   已支持
 * 当前对象字段  #this.xxx                            ✅   rootObject
 * 配置值占位符  ${server.port}                       ✅   placeholderHelper
 * Spring Bean 方法调用    @bean.method(...)        ✅   BeanFactoryResolver
 * 混合表达式   `"... + #param + @bean.xxx + ${}``   ✅   综合支持
 * 例如："'user:' + #testUser.id + '-' + @redissonLockController.testName(#testUser.id) + '-'  + #this.testName + '-' + ${server.port}"
 * @version 
 * @author 傅泉明
 */
public class ValueUtil {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAM_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final PropertyPlaceholderHelper PLACEHOLDER_HELPER = new PropertyPlaceholderHelper("${", "}");

    private ValueUtil() {}

    /**
     * 自定义标签:方法参数+当前对象字段+读取配置信息+Bean方法调用
     * ApplicationContext applicationContext
     * (ConfigurableBeanFactory) applicationContext.getAutowireCapableBeanFactory(),
     * (ConfigurableEnvironment) applicationContext.getEnvironment()
     * 
     * @param expression    需要转义的表达式
     * @param method
     * @param args
     * @param targetObject  支持：#this
     * @param beanFactory
     * @param environment
     * @return
     */
    public static String resolveExpression(String expression,
                                           Method method,
                                           Object[] args,
                                           Object targetObject,
                                           ConfigurableBeanFactory beanFactory,
                                           ConfigurableEnvironment environment) {
        // 1. 解析 ${} 配置占位符
        String resolved = PLACEHOLDER_HELPER.replacePlaceholders(expression, environment::getProperty);

        // 2. SpEL 上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 支持 #this
        context.setRootObject(targetObject);                          
        // 支持 @bean 调用
        context.setBeanResolver(new BeanFactoryResolver(beanFactory)); 
        // 获取方法参数名（需编译时保留参数名）
        // 在 Spring 项目中，并开启了 -g debug 编译（默认开启），虽然没有加 -parameters，Spring 仍然能在调试信息（local variable table）中读取参数名
        if (null != method && null != args) {
            String[] paramNames = PARAM_DISCOVERER.getParameterNames(method);
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    // 支持 #param
                    context.setVariable(paramNames[i], args[i]);          
                }
            }
        }

        // 3. SpEL 表达式解析
        Expression exp = PARSER.parseExpression(resolved);
        return exp.getValue(context, String.class);
    }
}
