//package com.fqm.framework.common.spring.util;
///**
// * 解决自定义标签读取配置信息，使用@Value读取
// * @Job(cron = "${app.job.cron}")
// * 
// * 1、@Value("${app.port}") 读取配置信息
// * 2、@Value("#{userService.count(${app.name})}") 调研容器执行带参数的方法
// * 3、@Value("#{${app.size} <= '12345'.length() ? ${app.size} : '12345'.length()}") 简单运算
// * 
// * @version 
// * @author 傅泉明
// */
//
//import java.util.Map;
//
//import org.springframework.beans.factory.config.BeanExpressionResolver;
//import org.springframework.beans.factory.config.ConfigurableBeanFactory;
//import org.springframework.context.expression.StandardBeanExpressionResolver;
//import org.springframework.expression.Expression;
//import org.springframework.expression.ExpressionParser;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//
//public class ValueUtil {
//
//    private static final BeanExpressionResolver RESSOLVER = new StandardBeanExpressionResolver();
//    
//    private static String EL_PREFIX = "#{";
//    private static String EL_SUFFIX = "}";
//    
//    private ValueUtil() {
//    }
//    /**
//     * 同时支持 Spring Bean 和 自定义变量的表达式解析
//     *
//     * @param beanFactory Spring Bean 工厂
//     * @param value       表达式（支持 #{bean.prop} 或 #{#param.prop}）
//     * @param variables   自定义变量（例如方法参数）
//     * @return 表达式结果
//     */
//    public static Object resolveExpression(ConfigurableBeanFactory beanFactory, String value, Map<String, Object> variables) {
//        if (value == null) {
//            return null;
//        }
//
//        // 先解析嵌套的 ${...} 占位符
//        String resolvedValue = beanFactory.resolveEmbeddedValue(value);
//
//        // 判断是否是 SpEL 表达式
//        if (resolvedValue != null && resolvedValue.startsWith(EL_PREFIX) && resolvedValue.endsWith(EL_SUFFIX)) {
//            try {
//                ExpressionParser parser = new SpelExpressionParser();
//                Expression expression = parser.parseExpression(resolvedValue);
//
//                // 创建 EvaluationContext，设置 beanFactory
//                StandardEvaluationContext context = new StandardEvaluationContext();
////                context.setBeanResolver(new org.springframework.expression.spel.support.BeanFactoryResolver(beanFactory));
//
//                // 注入自定义变量
//                if (variables != null) {
//                    for (Map.Entry<String, Object> entry : variables.entrySet()) {
//                        context.setVariable(entry.getKey(), entry.getValue());
//                    }
//                }
//
//                return expression.getValue(context);
//
//            } catch (Exception e) {
//                throw new IllegalArgumentException("表达式解析失败: " + resolvedValue, e);
//            }
//        }
//
//        // 非表达式则直接返回解析后的普通值
//        return resolvedValue;
//    }
//
//    /**
//     * 简化重载：无自定义变量
//     */
//    public static Object resolveExpression(ConfigurableBeanFactory beanFactory, String value) {
//        return resolveExpression(beanFactory, value, null);
//    }
//}
