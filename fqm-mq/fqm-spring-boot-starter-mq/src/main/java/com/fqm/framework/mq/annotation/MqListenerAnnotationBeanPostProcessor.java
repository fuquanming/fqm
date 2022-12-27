package com.fqm.framework.mq.annotation;

import com.fqm.framework.mq.listener.MqListenerParam;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @MqListener 注解监听，并转换为 List<MqListenerParam> 对象
 * 参考 RabbitListenerAnnotationBeanPostProcessor
 * 拦截 @ConfigurationProperties 配置为 mq.mqs 的所有配置，为@MqListener 中使用#{}，调用 @ConfigurationProperties 的方法准备，
 * 如果 @ConfigurationProperties 下的bean 通过 @Component 等注入到工厂，则beanName为类名：xxxProperties,
 * 如果 @ConfigurationProperties 下的bean 没有 @Component 等注入到工厂，则beanName为配置前缀+类全包名：mq.mqs.a-xx.xx.xxxProperties
 * @version 
 * @author 傅泉明
 */
public class MqListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

    /** 获取使用 @MqListener 的类及方法  */
    private List<MqListenerParam> listeners = new ArrayList<>();
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        final List<ListenerMethod> methods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<MqListener> listenerAnnotations = findListenerAnnotations(method);
            if (!listenerAnnotations.isEmpty()) {
                methods.add(new ListenerMethod(method, listenerAnnotations.toArray(new MqListener[listenerAnnotations.size()])));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
        
        if (!methods.isEmpty()) {
            for (ListenerMethod method : methods) {
                for (MqListener mqListener : method.annotations) {
                    // 消息名称
                    String name = mqListener.name();
                    Assert.isTrue(StringUtils.hasText(name), "Please specific [name] under @MqListener.");
                    MqListenerParam param = new MqListenerParam();
                    param.setName(name).setBean(bean).setMethod(method.method);
                    listeners.add(param);
                }
            }
        }

        return bean;
    }
    
    public List<MqListenerParam> getListeners() {
        return listeners;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }

    private Collection<MqListener> findListenerAnnotations(AnnotatedElement element) {
        return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY).stream(MqListener.class).map(MergedAnnotation::synthesize)
                .collect(Collectors.toList());
    }
    
    private static class ListenerMethod {

        final Method method; // NOSONAR

        final MqListener[] annotations; // NOSONAR

        ListenerMethod(Method method, MqListener[] annotations) { // NOSONAR
            this.method = method;
            this.annotations = annotations;
        }

    }

}
